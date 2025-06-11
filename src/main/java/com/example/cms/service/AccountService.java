package com.example.cms.service;

import com.example.cms.common.Constant;
import com.example.cms.common.DateUtil;
import com.example.cms.common.FileUtil;
import com.example.cms.common.ResponseCode;
import com.example.cms.config.JwtUtil;
import com.example.cms.dao.Account;
import com.example.cms.dao.ChucVu;
import com.example.cms.dao.HoSo;
import com.example.cms.dao.PhongBan;
import com.example.cms.dao.TrangThaiLamViec;
import com.example.cms.dao.UserInfo;
import com.example.cms.dto.base.Result;
import com.example.cms.dto.model.AccountInfoDTO;
import com.example.cms.dto.model.AccountsDTO;
import com.example.cms.dto.model.FileDto;
import com.example.cms.dto.request.CreateAccountInfoCrmRequest;
import com.example.cms.dto.request.CreateAccountInfoRequest;
import com.example.cms.dto.request.CrmVerifyCreateRequest;
import com.example.cms.dto.response.*;
import com.example.cms.feign.CRMService;
import com.example.cms.repository.AccountRepository;
import com.example.cms.repository.ChucVuRepository;
import com.example.cms.repository.HoSoRepository;
import com.example.cms.repository.PhongBanRepository;
import com.example.cms.repository.TrangThaiLamViecRepository;
import com.example.cms.repository.UserInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${spring.static-dir.avatar-user}")
    private String folderImgAvt;

    @Value("${spring.static-dir.file-user}")
    private String folderImgFile;

    @Value("${spring.application.domain}")
    private String domain;

    private final Logger logger = LogManager.getLogger(AccountService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AccountRepository accountRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;
    private final CRMService crmService;
    private final RedissonService redissonService;
    private final TrangThaiLamViecRepository trangThaiLamViecRepository;
    private final PhongBanRepository phongBanRepository;
    private final ChucVuRepository chucVuRepository;
    private final HoSoRepository hoSoRepository;
    private final HttpServletRequest request;
    private final FileUtil fileUtil;


    public Map<Object, Object> createAccount(String transactionId, String userRequest, MultipartFile avatar, List<MultipartFile> files) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            CreateAccountInfoRequest request = objectMapper.readValue(userRequest, CreateAccountInfoRequest.class);
            String username = generateUsername(request.getFullName());
            Account account = new Account();
            account.setUsername(username);
            // Chuỗi ký tự để tạo mật khẩu ngẫu nhiên
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            StringBuilder password = new StringBuilder(8);

            // Tạo chuỗi 8 ký tự ngẫu nhiên
            for (int i = 0; i < 8; i++) {
                password.append(characters.charAt(random.nextInt(characters.length())));
            }

            // Get an instance of MessageDigest for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Update digest with the input string
            md.update(password.toString().getBytes());

            // Get the MD5 hash
            byte[] mdBytes = md.digest();

            // Convert byte array to a hexadecimal string
            String md5String = bytesToHex(mdBytes);

            // Hash mật khẩu bằng BCrypt
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            account.setPassword(encoder.encode(md5String));
            account.setIsFirstPassword(true);
            account.setTypeAccount(request.getTypeAccount());
            account.setCreatedAt(DateUtil.genCreatedAt(null));
            account.setUpdatedAt(DateUtil.genCreatedAt(null));

            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(username);
            userInfo.setMaNv(generateMaNv());
            userInfo.setFullName(request.getFullName());
            userInfo.setDiaChiHienTai(request.getDiaChiHienTai());
            userInfo.setBirth(request.getBirth().toString());
            userInfo.setIdPhongBan(request.getIdPhongBan());
            userInfo.setIdChucVu(request.getIdChucVu());
            userInfo.setIdTrangThai(request.getIdTrangThai());

            List<ObjectId> hoSoIDs = new ArrayList<>();
            userInfo.setSoCCCD(request.getSoCCCD());
            userInfo.setWorkStartDate(request.getWorkStartDate().toString());
            userInfo.setGender(request.getGender());
            userInfo.setPhone(request.getPhone());
            userInfo.setEmail(request.getEmail());
            userInfo.setNumberOfDaysOffRemaining(0.0);
            userInfo.setCreatedAt(DateUtil.genCreatedAt(null));
            userInfo.setUpdatedAt(DateUtil.genCreatedAt(null));

            CreateAccountInfoCrmRequest createAccountInfoCrmRequest = new CreateAccountInfoCrmRequest();
            createAccountInfoCrmRequest.setFullName(request.getFullName());
            createAccountInfoCrmRequest.setEmail(request.getEmail());
            createAccountInfoCrmRequest.setBirth(request.getBirth());
            createAccountInfoCrmRequest.setPhone(request.getPhone());
            createAccountInfoCrmRequest.setAddress(request.getDiaChiHienTai());
            createAccountInfoCrmRequest.setPosition(chucVuRepository.findById(String.valueOf(request.getIdChucVu())).get().getTenChucVu());
            createAccountInfoCrmRequest.setWorkingStatus(trangThaiLamViecRepository.findById(String.valueOf(request.getIdTrangThai())).get().getTenTrangThai());
            createAccountInfoCrmRequest.setDepartment(phongBanRepository.findById(String.valueOf(request.getIdPhongBan())).get().getTenPhongBan());

            String data = crmService.genToken(createAccountInfoCrmRequest);
            ObjectMapper objectMapper = new ObjectMapper();
            CRMResponseDataDTO response = objectMapper.readValue(data, CRMResponseDataDTO.class);
            if (response.getCode().equalsIgnoreCase("200")) {
                CrmVerifyCreateRequest request1 = new CrmVerifyCreateRequest();
                request1.setToken(response.getData().toString());
                String data1 = crmService.verifyCreate(request1);
                ObjectMapper objectMapper1 = new ObjectMapper();
                CRMResponseDataDTO response1 = objectMapper1.readValue(data1, CRMResponseDataDTO.class);
                if (response1.getCode().equalsIgnoreCase("200")) {
                    //lưu mới
                    List<String> mappings = new ArrayList<>();
                    for(MultipartFile file : files){
                        String mappingImgId = Constant.HRM.concat(username.concat(Objects.requireNonNull(file.getOriginalFilename())));
                        mappings.add(mappingImgId);
                    }
                    files.add(avatar);
                    mappings.add(Constant.HRM.concat(username.concat(Objects.requireNonNull(avatar.getOriginalFilename()))));
                    FileResponse fileResponse = fileUtil.writeFile(files, mappings);

                    if (fileResponse.getSuccess()  && !fileResponse.getFiles().isEmpty()) {
                        for (int i = 0; i < fileResponse.getFiles().size() - 1; i++) {
                            HoSo hoSo = new HoSo();
                            hoSo.setImageId(fileResponse.getFiles().get(i).getFileId());
                            hoSo.setType(fileResponse.getFiles().get(i).getType());
                            hoSo.setCreatedAt(DateUtil.genCreatedAt(null));
                            hoSo.setUpdatedAt(DateUtil.genCreatedAt(null));
                            hoSoRepository.save(hoSo);
                            hoSoIDs.add(hoSo.getId());
                        }
                        if(!avatar.isEmpty()){
                            userInfo.setAvatarId(fileResponse.getFiles().get(fileResponse.getFiles().size()-1).getFileId());
                        }
                        userInfo.setIdHoSo(hoSoIDs);
                        accountRepository.save(account);
                        userInfoRepository.save(userInfo);
                    }
                }
            }
            Map<String, String> mapData = new HashMap<>();
            mapData.put("username", username);
            mapData.put("password", password.toString());
            resultExecute.put(Constant.RESPONSE_KEY.DATA, mapData);
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện thêm mới người dùng! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> updateAccount(String transactionId, ObjectId accountId, String userRequest, MultipartFile avatar, List<MultipartFile> files) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();
        try {
            // Kiểm tra token
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }
            CreateAccountInfoRequest request = objectMapper.readValue(userRequest, CreateAccountInfoRequest.class);
            Account account = accountRepository.findById(String.valueOf(accountId)).orElse(null);
            if (account != null) {
                account.setUpdatedAt(new Date().toString());
                account.setTypeAccount(request.getTypeAccount());

                UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), false);
                userInfo.setFullName(request.getFullName());
                userInfo.setDiaChiHienTai(request.getDiaChiHienTai());
                userInfo.setBirth(request.getBirth().toString());
                userInfo.setIdPhongBan(request.getIdPhongBan());
                userInfo.setIdChucVu(request.getIdChucVu());
                userInfo.setIdTrangThai(request.getIdTrangThai());
//                String folderNameAvt = folderImgAvt;
//                File fileAvt = new File(folderNameAvt);
//                if (!fileAvt.exists()) fileAvt.mkdir();
//                String folderNameFile = folderImgFile;
//                File fileFile = new File(folderNameFile);
//                if (!fileFile.exists()) fileFile.mkdir();
                userInfo.setSoCCCD(request.getSoCCCD());
                userInfo.setWorkStartDate(request.getWorkStartDate().toString());
                userInfo.setGender(request.getGender());
                userInfo.setPhone(request.getPhone());
                userInfo.setEmail(request.getEmail());
                userInfo.setCreatedAt(DateUtil.genCreatedAt(null));
                userInfo.setUpdatedAt(DateUtil.genCreatedAt(null));

//                // Lấy tên gốc của file và tạo tên duy nhất
////                if (avatar != null) {
////                    String originalFilename = Objects.requireNonNull(avatar.getOriginalFilename());
////                    if (!Constant.URL_IMG_AVATAR.concat(originalFilename).equals(userInfo.getAvatarUrl())) {
////                        String uniqueFilename = getUniqueFilename(folderNameAvt, originalFilename);
////                        String avatarUrl = saveImage(folderNameAvt, avatar, uniqueFilename);
////                        if (StringUtils.isNotEmpty(avatarUrl)) {
////                            if (!userInfo.getAvatarUrl().isEmpty()) {
////                                // Xóa file vật lý nếu cần
////                                File fileToDelete = new File(folderNameAvt + "/" + userInfo.getAvatarUrl().substring(Constant.URL_IMG_AVATAR.length()));
////                                if (fileToDelete.exists()) {
////                                    fileToDelete.delete();
////                                }
////                            }
////                            userInfo.setAvatarUrl(Constant.URL_IMG_AVATAR.concat(uniqueFilename));
////                        }
////                    }
////                } else {
////                    if (!userInfo.getAvatarUrl().isEmpty()) {
////                        // Xóa file vật lý nếu cần
////                        File fileToDelete = new File(folderNameAvt + "/" + userInfo.getAvatarUrl().substring(Constant.URL_IMG_AVATAR.length()));
////                        if (fileToDelete.exists()) {
////                            fileToDelete.delete();
////                        }
////                    }
////                    userInfo.setAvatarUrl("");
////                }
////                List<ObjectId> hoSoID1s = new ArrayList<>();
////                List<ObjectId> hoSoIDs = new ArrayList<>(userInfo.getIdHoSo() == null ? hoSoID1s : userInfo.getIdHoSo());
////                if (files != null && files.toArray().length > 0) {
////                    List<String> urls = new ArrayList<>();
////                    if (request.getFilesOld() != null) {
////                        for (String urlOld : request.getFilesOld()) {
////                            String url = urlOld.substring(domain.length());
////                            urls.add(url);
////                        }
////                    }
////
////                    // Lấy danh sách path của các file được upload từ MultipartFile[]
////                    Set<String> uploadedFilePaths = files.stream()
////                            .filter(file -> file != null && !file.isEmpty())
////                            .map(file -> Constant.URL_IMG_FILE.concat(Objects.requireNonNull(file.getOriginalFilename())))
////                            .collect(Collectors.toSet());
////
////                    uploadedFilePaths.addAll(urls);
////
////                    // Kiểm tra và xóa các HoSo cũ không còn trong danh sách upload
////                    List<ObjectId> idsToRemove = new ArrayList<>();
////                    for (ObjectId idHoSo : userInfo.getIdHoSo() == null ? hoSoID1s : userInfo.getIdHoSo()) {
////                        HoSo hoSo = hoSoRepository.findById(String.valueOf(idHoSo)).orElse(null);
////                        if (hoSo != null && !uploadedFilePaths.contains(hoSo.getPath())) {
////                            // Xóa file vật lý nếu cần
////                            File fileToDelete = new File(folderNameFile + "/" + hoSo.getName());
////                            if (fileToDelete.exists()) {
////                                fileToDelete.delete();
////                            }
////                            // Xóa bản ghi HoSo khỏi MongoDB
////                            hoSoRepository.delete(hoSo);
////                            idsToRemove.add(idHoSo); // Đánh dấu để xóa khỏi hoSoIDs
////                        }
////                    }
////                    hoSoIDs.removeAll(idsToRemove); // Cập nhật hoSoIDs sau khi xóa
////
////                    // Xử lý các file được upload
////                    for (MultipartFile file : files) {
////                        if (file == null || file.isEmpty()) {
////                            continue; // Bỏ qua nếu file rỗng
////                        }
////
////                        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
////                        String filePath = Constant.URL_IMG_FILE.concat(originalFilename);
////
////                        // Kiểm tra xem file đã tồn tại chưa
////                        boolean fileExists = false;
////                        for (ObjectId idHoSo : userInfo.getIdHoSo() == null ? hoSoID1s : userInfo.getIdHoSo()) {
////                            HoSo hoSo = hoSoRepository.findById(String.valueOf(idHoSo)).orElse(null);
////                            if (hoSo != null && filePath.equals(hoSo.getPath())) {
////                                fileExists = true;
////                                break;
////                            }
////                        }
////
////                        // Nếu file không tồn tại, lưu file và tạo HoSo mới
////                        if (!fileExists) {
////                            String uniqueFilename = getUniqueFilename(folderNameFile, originalFilename);
////                            saveImage(folderNameFile, file, uniqueFilename);
////
////                            HoSo hoSo1 = new HoSo();
////                            hoSo1.setId(new ObjectId());
////                            hoSo1.setName(uniqueFilename);
////                            hoSo1.setPath(Constant.URL_IMG_FILE.concat(uniqueFilename));
////                            hoSo1.setCreatedAt(DateUtil.genCreatedAt(null));
////                            hoSo1.setUpdatedAt(DateUtil.genCreatedAt(null));
////                            hoSo1.setType(getFileExtension(uniqueFilename));
////                            String unit = convertMultipartFileToUnit(file);
////                            hoSo1.setCapacity(unit);
////
////                            // Lưu vào MongoDB và thêm ID vào danh sách
////                            hoSoRepository.save(hoSo1);
////                            hoSoIDs.add(hoSo1.getId());
////                        }
////                    }
////                }
////                if (files == null && (request.getFilesOld() == null || request.getFilesOld().isEmpty())) {
////                    for (ObjectId idHoSo : userInfo.getIdHoSo() == null ? hoSoID1s : userInfo.getIdHoSo()) {
////                        HoSo hoSo = hoSoRepository.findById(String.valueOf(idHoSo)).orElse(null);
////                        if (hoSo != null) {
////                            // Xóa file vật lý nếu cần
////                            File fileToDelete = new File(folderNameFile + "/" + hoSo.getName());
////                            if (fileToDelete.exists()) {
////                                fileToDelete.delete();
////                            }
////                            // Xóa bản ghi HoSo khỏi MongoDB
////                            hoSoRepository.delete(hoSo);
////                        }
////                    }
////                    hoSoIDs.clear();
////                }
////                userInfo.setIdHoSo(hoSoIDs);
////                accountRepository.save(account);
////                userInfoRepository.save(userInfo);
                List<ObjectId> hoSoIDs = userInfo.getIdHoSo() != null ? new ArrayList<>(userInfo.getIdHoSo()) : new ArrayList<>();

                // Xử lý avatar + files thông qua fileUtil
                if ((files != null && !files.isEmpty()) || (avatar != null && !avatar.isEmpty())) {
                    List<MultipartFile> allFiles = new ArrayList<>();
                    List<String> mappings = new ArrayList<>();

                    String username = account.getUsername();

                    if (files != null) {
                        for (MultipartFile file : files) {
                            allFiles.add(file);
                            mappings.add(Constant.HRM.concat(username.concat(Objects.requireNonNull(file.getOriginalFilename()))));
                        }
                    }

                    if (avatar != null && !avatar.isEmpty()) {
                        allFiles.add(avatar);
                        mappings.add(Constant.HRM.concat(username.concat(Objects.requireNonNull(avatar.getOriginalFilename()))));
                    }

                    // Gọi writeFile
                    FileResponse fileResponse = fileUtil.writeFile(allFiles, mappings);
                    if (fileResponse.getSuccess() && !fileResponse.getFiles().isEmpty()) {
                        // Xóa các HoSo cũ trước
                        if (userInfo.getIdHoSo() != null) {
                            for (ObjectId id : userInfo.getIdHoSo()) {
                                hoSoRepository.deleteById(String.valueOf(id));
                            }
                        }
                        hoSoIDs.clear();

                        for (int i = 0; i < fileResponse.getFiles().size(); i++) {
                            FileDto fileDetail = fileResponse.getFiles().get(i);

                            // Nếu là avatar (file cuối cùng)
                            if (avatar != null && !avatar.isEmpty() && i == fileResponse.getFiles().size() - 1) {
                                userInfo.setAvatarId(fileDetail.getFileId());
                            } else {
                                HoSo hoSo = new HoSo();
                                hoSo.setImageId(fileDetail.getFileId());
                                hoSo.setType(fileDetail.getType());
                                hoSo.setCreatedAt(DateUtil.genCreatedAt(null));
                                hoSo.setUpdatedAt(DateUtil.genCreatedAt(null));
                                hoSoRepository.save(hoSo);
                                hoSoIDs.add(hoSo.getId());
                            }
                        }
                    }
                } else {
                    // Nếu người dùng đã xoá hết files + avatar
                    if (userInfo.getIdHoSo() != null) {
                        for (ObjectId id : userInfo.getIdHoSo()) {
                            hoSoRepository.deleteById(String.valueOf(id));
                        }
                    }
                    hoSoIDs.clear();
                    userInfo.setAvatarId(null);
                }

                userInfo.setIdHoSo(hoSoIDs);
                accountRepository.save(account);
                userInfoRepository.save(userInfo);
                resultExecute.put(Constant.RESPONSE_KEY.DATA, account);
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra ngoại lệ khi thực hiện update người dùng! Rootcause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }
        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    private String generateUsername(String fullname) {
        // Tách họ, tên đệm, tên
        String[] parts = fullname.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Fullname must have at least two parts (e.g., 'Trần Minh')");
        }

        // Lấy tên (phần cuối) và chuyển thành không dấu
        String lastName = removeDiacritics(parts[parts.length - 1]); // "Quý" -> "Quy"

        // Lấy chữ cái đầu của họ + tên đệm
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                initials.append(removeDiacritics(parts[i]).charAt(0)); // "P" từ "Phạm", "M" từ "Minh"
            }
        }

        // Username gốc: tên không dấu + chữ cái đầu (e.g., "QuyPM")
        String baseUsername = lastName + initials;

        // Kiểm tra trùng lặp
        List<Account> existingAccounts = accountRepository.findByUsernameStartingWith(baseUsername);
        if (existingAccounts.isEmpty()) {
            return baseUsername; // Không trùng, trả về username gốc
        } else {
            // Đếm số người trùng tên và thêm số thứ tự
            int count = existingAccounts.size();
            return baseUsername + count; // e.g., "QuyPM1" nếu đã có "QuyPM"
        }
    }

    // Hàm loại bỏ dấu tiếng Việt
    private String removeDiacritics(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    // Chuyển pass mới sang md5
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateMaNv() {
        // Lấy mã nhân viên lớn nhất từ DB
        UserInfo latestAccount = userInfoRepository.findTopByOrderByMaNvDesc();
        int nextNumber;

        if (latestAccount == null || latestAccount.getMaNv() == null) {
            nextNumber = 1; // Bắt đầu từ 0001 nếu chưa có mã nào
        } else {
            nextNumber = Integer.parseInt(latestAccount.getMaNv()) + 1; // Tăng lên 1
        }

        // Định dạng: nếu nhỏ hơn 10000 thì giữ 4 chữ số với số 0 ở đầu, nếu không thì để nguyên
        if (nextNumber < 10000) {
            return String.format("%04d", nextNumber); // "0001", "0012", ..., "9999"
        } else {
            return String.valueOf(nextNumber); // "10000", "10001", ...
        }
    }

    private String saveImage(String rootFolder, MultipartFile file, String fileName) {
        try {
            BufferedImage bi = ImageIO.read(file.getInputStream());
            File legalFrontImg = new File(rootFolder.concat(Constant.SPECIAL_CHAR.SLASH).concat(fileName));
            ImageIO.write(bi, Constant.IMAGE_EXTENSION.PNG, legalFrontImg);
            return legalFrontImg.getAbsolutePath();
        } catch (Exception ex) {
            logger.warn("Xảy ra lỗi khi đồng bộ ảnh fileName: {}", fileName);
            return Constant.SPECIAL_CHAR.EMPTY;
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String convertMultipartFileToUnit(MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                long sizeInBytes = file.getSize();
                double sizeInKB = (double) sizeInBytes / 1024;
                double sizeInMB = sizeInKB / 1024;
                double sizeInGB = sizeInMB / 1024;
                if (sizeInGB >= 1) {
                    return String.format("%.2f GB", sizeInGB);
                } else if (sizeInMB >= 1) {
                    return String.format("%.2f MB", sizeInMB);
                } else if (sizeInKB >= 1) {
                    return String.format("%.2f KB", sizeInKB);
                } else {
                    return sizeInBytes + " bytes";
                }
            } else {
                return "0 bytes";
            }
        } catch (Exception ex) {
            logger.error("Error converting file size to unit: {}", ex.getMessage());
            return "N/A";
        }
    }

    // Hàm tạo tên file duy nhất nếu trùng
    private String getUniqueFilename(String folderPath, String originalFilename) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        String name = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = originalFilename;
        int count = 0;

        // Kiểm tra trùng lặp file và tạo tên mới
        while (new File(folderPath + "/" + newFilename).exists()) {
            count++;
            newFilename = name + count + extension; // Ví dụ: avatar1.jpg, avatar2.jpg
        }

        return newFilename;
    }

    public Map<Object, Object> getInfoWeb(String transactionId, String accountId) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();

        try {
            // Kiểm tra token admin
            if (!jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            // Lấy thông tin tài khoản
            Account account = accountRepository.findById(accountId).orElse(null);
            if (account == null) {
                result = new Result(ResponseCode.DATA_NOT_FOUND.getCode(), false, "Không tìm thấy tài khoản");
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            UserInfo userInfo = userInfoRepository.findByUsernameAndIsDelete(account.getUsername(), Constant.STATUS.IS_UN_DELETED);
            if (userInfo == null) {
                result = new Result(ResponseCode.DATA_NOT_FOUND.getCode(), false, "Không tìm thấy thông tin UserInfo");
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            // Định dạng ngày tháng
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); // Định dạng input
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Định dạng output

            AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
            accountInfoDTO.setAccountId(String.valueOf(account.getId()));
            accountInfoDTO.setMaNv(userInfo.getMaNv());
            accountInfoDTO.setFullName(userInfo.getFullName());
            accountInfoDTO.setSoCCCD(userInfo.getSoCCCD());
            accountInfoDTO.setGender(userInfo.getGender());
            accountInfoDTO.setDiaChiHienTai(userInfo.getDiaChiHienTai());
            accountInfoDTO.setAvatar(domain.concat(userInfo.getAvatarUrl()));
            accountInfoDTO.setTypeAccount(account.getTypeAccount());
            List<String> hoSos = new ArrayList<>();
            if (userInfo.getIdHoSo() != null) {
                for (ObjectId id : userInfo.getIdHoSo()) {
                    HoSo hoSo = hoSoRepository.findByIdAndIsDelete(id, false);
                    if (hoSo != null) {
                        hoSos.add(domain.concat(hoSo.getPath()));
                    }
                }
            }

            accountInfoDTO.setFiles(hoSos);

            // Chuyển đổi birthDate (String → LocalDate → String)
            if (userInfo.getBirth() != null && !userInfo.getBirth().isEmpty()) {
                try {
                    LocalDate birthDate = LocalDate.parse(userInfo.getBirth(), inputFormatter);
                    accountInfoDTO.setBirth(birthDate.format(outputFormatter));
                } catch (DateTimeParseException e) {
                    logger.warn("Lỗi khi parse birthDate: {}", userInfo.getBirth());
                    accountInfoDTO.setBirth(""); // Trả về chuỗi rỗng nếu lỗi
                }
            }

            // Chuyển đổi workStartDate (String → LocalDate → String)
            if (userInfo.getWorkStartDate() != null && !userInfo.getWorkStartDate().isEmpty()) {
                try {
                    LocalDate workStartDate = LocalDate.parse(userInfo.getWorkStartDate(), inputFormatter);
                    accountInfoDTO.setWorkStartDate(workStartDate.format(outputFormatter));
                } catch (DateTimeParseException e) {
                    logger.warn("Lỗi khi parse workStartDate: {}", userInfo.getWorkStartDate());
                    accountInfoDTO.setWorkStartDate("");
                }
            }

            accountInfoDTO.setPhone(userInfo.getPhone());
            accountInfoDTO.setEmail(userInfo.getEmail());

            // Lấy thông tin phòng ban, chức vụ, trạng thái làm việc
            TrangThaiLamViec trangThaiLamViec = trangThaiLamViecRepository.findById(String.valueOf(userInfo.getIdTrangThai())).orElse(null);
            if (trangThaiLamViec != null) {
                TrangThaiLamViecResponse trangThaiLamViecResponse = new TrangThaiLamViecResponse();
                trangThaiLamViecResponse.setId(String.valueOf(trangThaiLamViec.getId()));
                trangThaiLamViecResponse.setTenTrangThai(trangThaiLamViec.getTenTrangThai());
                accountInfoDTO.setTrangThai(trangThaiLamViecResponse);
            }


            PhongBan phongBan = phongBanRepository.findById(String.valueOf(userInfo.getIdPhongBan())).orElse(null);
            if (phongBan != null) {
                PhongBanResponse phongBanResponse = new PhongBanResponse();
                phongBanResponse.setId(String.valueOf(phongBan.getId()));
                phongBanResponse.setTenPhongBan(phongBan.getTenPhongBan());
                accountInfoDTO.setPhongBan(phongBanResponse);
            }

            ChucVu chucVu = chucVuRepository.findById(String.valueOf(userInfo.getIdChucVu())).orElse(null);
            if (chucVu != null) {
                ChucVuResponse chucVuResponse = new ChucVuResponse();
                chucVuResponse.setId(String.valueOf(chucVu.getId()));
                chucVuResponse.setTenChucVu(chucVu.getTenChucVu());
                accountInfoDTO.setChucVu(chucVuResponse);
            }

            accountInfoDTO.setOffice("");
            accountInfoDTO.setJobPosition("");

            resultExecute.put(Constant.RESPONSE_KEY.DATA, accountInfoDTO);
        } catch (Exception ex) {
            logger.error("transactionId: {} - Lỗi khi lấy thông tin tài khoản! Root cause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }

        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    public Map<Object, Object> getList(String transactionId, Integer pageSize, Integer pageNumber, String token, Boolean isDelete, String maNv, String fullName, String soCCCD, Boolean gender, String phone, String email, ObjectId phongBan, ObjectId chucVu, LocalDate fromDate, LocalDate toDate) {
        Map<Object, Object> resultExecute = new HashMap<>();
        Result result = Result.OK();

        try {
            // Kiểm tra token
            if (token == null && !jwtUtil.isAdminToken(jwtUtil.getToken(request))) {
                result = new Result(ResponseCode.TOKEN_INVALID.getCode(), false, ResponseCode.TOKEN_INVALID.getMessage());
                resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
                return resultExecute;
            }

            // Phân trang bằng Pageable
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            Page<UserInfo> userInfoPage = findAll(isDelete, maNv, fullName, soCCCD, gender, phone, email, phongBan, chucVu, fromDate, toDate, pageable);
            if (!userInfoPage.isEmpty()) {
                List<AccountInfoDTO> accountInfoDTOs = userInfoPage.getContent().stream()
                        .map(account -> {
                            Account account1 = accountRepository.findTopByUsernameAndIsDelete(account.getUsername(), false);
                            if (account1 != null){
                                AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
                                accountInfoDTO.setAccountId(String.valueOf(account1.getId()));

                                accountInfoDTO.setMaNv(account.getMaNv());
                                accountInfoDTO.setFullName(account.getFullName());
                                accountInfoDTO.setUserName(account.getUsername());
                                accountInfoDTO.setTypeAccount(account1.getTypeAccount());

                                // Lấy thông tin phòng ban, chức vụ, trạng thái làm việc
                                TrangThaiLamViec trangThaiLamViec = trangThaiLamViecRepository.findById(String.valueOf(account.getIdTrangThai())).orElse(null);
                                if (trangThaiLamViec != null) {
                                    TrangThaiLamViecResponse trangThaiLamViecResponse = new TrangThaiLamViecResponse();
                                    trangThaiLamViecResponse.setId(String.valueOf(trangThaiLamViec.getId()));
                                    trangThaiLamViecResponse.setTenTrangThai(trangThaiLamViec.getTenTrangThai());
                                    accountInfoDTO.setTrangThai(trangThaiLamViecResponse);
                                }


                                PhongBan phongBan1 = phongBanRepository.findById(String.valueOf(account.getIdPhongBan())).orElse(null);
                                if (phongBan1 != null) {
                                    PhongBanResponse phongBanResponse = new PhongBanResponse();
                                    phongBanResponse.setId(String.valueOf(phongBan1.getId()));
                                    phongBanResponse.setTenPhongBan(phongBan1.getTenPhongBan());
                                    accountInfoDTO.setPhongBan(phongBanResponse);
                                }

                                ChucVu chucVu1 = chucVuRepository.findById(String.valueOf(account.getIdChucVu())).orElse(null);
                                if (chucVu1 != null) {
                                    ChucVuResponse chucVuResponse = new ChucVuResponse();
                                    chucVuResponse.setId(String.valueOf(chucVu1.getId()));
                                    chucVuResponse.setTenChucVu(chucVu1.getTenChucVu());
                                    accountInfoDTO.setChucVu(chucVuResponse);
                                }

                                return accountInfoDTO;
                            }
                            return null;
                        })
                        .collect(Collectors.toList());

                AccountsDTO accountsDTO = new AccountsDTO();
                accountsDTO.setAccountInfoDTOS(accountInfoDTOs);
                accountsDTO.setTotalRecords(userInfoPage.getTotalElements());

                resultExecute.put(Constant.RESPONSE_KEY.DATA, accountsDTO);
            }
        } catch (Exception ex) {
            logger.error("transactionId: {} - xảy ra lỗi khi lấy danh sách người dùng! Root cause: {}", transactionId, ex);
            result = new Result(ResponseCode.SYSTEM.getCode(), false, ResponseCode.SYSTEM.getMessage());
        }

        resultExecute.put(Constant.RESPONSE_KEY.RESULT, result);
        return resultExecute;
    }

    private final MongoTemplate mongoTemplate;

    public Page<UserInfo> findAll(Boolean isDelete, String maNv, String fullName, String soCCCD,
                                  Boolean gender, String phone, String email, ObjectId phongBan, ObjectId chucVu, LocalDate fromDate, LocalDate toDate,
                                  Pageable pageable) {
        Query query = new Query();

        if (isDelete != null) {
            query.addCriteria(Criteria.where("IS_DELETE").is(isDelete)); // Sửa thành IS_DELETE
        }
        if (maNv != null) query.addCriteria(Criteria.where("MA_NV").is(maNv)); // Sửa thành MA_NV
        if (fullName != null)
            query.addCriteria(Criteria.where("FULL_NAME").regex(fullName, "i")); // Sửa thành FULL_NAME
        if (soCCCD != null) query.addCriteria(Criteria.where("SO_CCCD").is(soCCCD)); // Sửa thành SO_CCCD
        if (gender != null) query.addCriteria(Criteria.where("GENDER").is(gender)); // Sửa thành GENDER
        if (phone != null) query.addCriteria(Criteria.where("PHONE").is(phone)); // Sửa thành PHONE
        if (email != null) query.addCriteria(Criteria.where("EMAIL").is(email)); // Sửa thành EMAIL
        if (phongBan != null) query.addCriteria(Criteria.where("ID_PHONG_BAN").is(phongBan)); // Sửa thành PHONE
        if (chucVu != null) query.addCriteria(Criteria.where("ID_CHUC_VU").is(chucVu)); // Sửa thành EMAIL
        if (fromDate != null && toDate != null) {

            String dateRegex = DateUtil.buildDateRegex(fromDate, toDate);
            query.addCriteria(Criteria.where("CREATED_AT").regex(dateRegex, "i"));
        }

        long total = mongoTemplate.count(query, UserInfo.class);
        query.with(pageable);
        List<UserInfo> userInfos = mongoTemplate.find(query, UserInfo.class);

        return new PageImpl<>(userInfos, pageable, total);
    }

}

# Auth Client Spring Boot Starter 

Client library for easy integration with the Auth Service Platform.

Thư viện này cung cấp một cách đơn giản và mạnh mẽ để các ứng dụng Spring Boot khác có thể tương tác và bảo mật API bằng dịch vụ xác thực tập trung của bạn.

---

### --- Tính năng ---

1.  **SDK Client (`AuthClient`):** Cung cấp một Service Bean được cấu hình sẵn để bạn có thể gọi các API quản lý (EndUser, ProjectRole) của dịch vụ xác thực chỉ bằng các phương thức Java đơn giản.
2.  **Bảo mật bằng Annotation (`@RequiresProjectRole`):** Dễ dàng bảo vệ các API của bạn, yêu cầu `EndUser` phải có một vai trò (role) nhất định trong project để có thể truy cập.

---

### --- Cài đặt ---

Thêm dependency sau vào file `pom.xml` trong project Spring Boot của bạn:

```xml
<dependency>
    <groupId>io.github.kietvo5924</groupId>
    <artifactId>auth-client-spring-boot-starter</artifactId>
    <version>2.2.1</version>
</dependency>
```

---

### --- Cấu hình ---

Trong file `application.properties` (hoặc `.yml`) của bạn, hãy cung cấp các thuộc tính sau:

```properties
# URL của dịch vụ xác thực chính
# auth.client.base-url=[https://auth-service-platform.onrender.com](https://auth-service-platform.onrender.com) # (Tùy chọn, đây là giá trị mặc định)

# API Key công khai của Project.
# Bắt buộc cho tất cả các chức năng.
auth.client.api-key=YOUR_PROJECT_API_KEY_HERE
```

---

### --- Cách sử dụng ---

#### **1. Dùng SDK `AuthClient` để Quản lý**

Sau khi cấu hình, bạn có thể tiêm (`inject`) bean `AuthClient` vào bất kỳ service hoặc controller nào và bắt đầu sử dụng. Dưới đây là danh sách đầy đủ các phương thức có sẵn:

**Luồng Xác thực Công khai cho End-User**
Các phương thức này dùng apiKey đã được cấu hình và không yêu cầu token.


* `AuthResponse loginEndUser(EndUserLoginRequest request)`: Đăng nhập cho EndUser và trả về accessToken.

* `ApiResponse registerEndUser(EndUserRegisterRequest request)`: Đăng ký một EndUser mới và gửi email xác thực.

* `ApiResponse forgotPassword(ForgotPasswordRequest request)`: Bắt đầu luồng quên mật khẩu bằng cách gửi OTP qua email.

* `ApiResponse resetPassword(ResetPasswordRequest request)`: Đặt lại mật khẩu mới cho EndUser bằng email và mã OTP.


**Tương tác với API Cá nhân của End-User (Đã đăng nhập)**
Các phương thức này yêu cầu bạn phải truyền vào accessToken của EndUser.


* `EndUserResponse getMyProfile(String endUserToken)`: Lấy thông tin cá nhân của EndUser đang đăng nhập.

* `EndUserResponse updateMyProfile(String endUserToken, UpdateMyProfileRequest request)`: EndUser tự cập nhật fullName của mình.

* `ApiResponse changeMyPassword(String endUserToken, ChangePasswordRequest request)`: EndUser tự đổi mật khẩu (yêu cầu mật khẩu cũ và mới).


**Quản lý End-User trong Project (Bởi Owner)**
Các phương thức này yêu cầu ownerToken phải được cấu hình.


* `List<EndUserResponse> getEndUsersForProject(Long projectId)`: Lấy danh sách tất cả EndUser trong một project.

* `EndUserResponse updateEndUserDetails(Long projectId, Long endUserId, UpdateEndUserRequest request)`: Cập nhật fullName cho một EndUser.

* `void lockEndUser(Long projectId, Long endUserId)`: Khóa tài khoản của một EndUser.

* `void unlockEndUser(Long projectId, Long endUserId)`: Mở khóa tài khoản của một EndUser.

* `EndUserResponse updateUserRoles(Long projectId, Long endUserId, UpdateEndUserRolesRequest request)`: Thay thế toàn bộ danh sách vai trò của một EndUser.

* `EndUserResponse addRolesToUser(Long projectId, Long endUserId, UpdateEndUserRolesRequest request)`: Thêm một hoặc nhiều vai trò mới cho EndUser.

* `void removeRoleFromUser(Long projectId, Long endUserId, Long roleId)`: Xóa một vai trò cụ thể khỏi EndUser.


**Quản lý Vai trò trong Project (Bởi Owner)**
Các phương thức này yêu cầu ownerToken phải được cấu hình.


* `List<ProjectRoleResponse> getProjectRoles(Long projectId)`: Lấy danh sách tất cả các vai trò của một project.

* `ProjectRoleResponse createProjectRole(Long projectId, ProjectRoleRequest request)`: Tạo một vai trò mới cho project.

* `ProjectRoleResponse updateProjectRole(Long projectId, Long roleId, ProjectRoleRequest request)`: Cập nhật tên và cấp bậc (level) của một vai trò.

* `void deleteProjectRole(Long projectId, Long roleId)`: Xóa một vai trò khỏi project (nếu không có user nào đang sử dụng).

**Ví dụ:**
```java
import com.authplatform.client.service.AuthClient;
import com.authplatform.client.dto.*; // Import các DTO cần thiết
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MyProjectManagementService {

    private final AuthClient authClient;

    public MyProjectManagementService(AuthClient authClient) {
        this.authClient = authClient;
    }

    // Lấy danh sách tất cả EndUser trong project
    public List<EndUserResponse> getAllUsers() {
        return authClient.getEndUsersForProject(myProjectId);
    }

    // Khóa một EndUser
    public void lockAUser(Long userId) {
        authClient.lockEndUser(myProjectId, userId);
    }

    // Tạo một vai trò mới
    public ProjectRoleResponse createNewRole() {
        ProjectRoleRequest newRole = new ProjectRoleRequest();
        newRole.setName("VIP_USER");
        newRole.setLevel(500);
        return authClient.createProjectRole(myProjectId, newRole);
    }
}
```

#### **2. Dùng Annotation `@RequiresProjectRole` để Bảo vệ API**

Để bảo vệ một API và chỉ cho phép các `EndUser` có vai trò nhất định truy cập, hãy sử dụng annotation `@RequiresProjectRole`. Thư viện sẽ tự động kiểm tra token `Authorization: Bearer <end_user_token>` trong header của request.

**Ví dụ:**
```java
import com.authplatform.client.security.RequiresProjectRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyApiController {

    // API này yêu cầu EndUser phải có vai trò "USER" hoặc "ADMIN"
    @GetMapping("/api/data")
    @RequiresProjectRole({"USER", "ADMIN"})
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok("Some protected data for users.");
    }

    // API này chỉ yêu cầu EndUser có vai trò "ADMIN"
    @GetMapping("/api/admin/data")
    @RequiresProjectRole("ADMIN")
    public ResponseEntity<String> getAdminData() {
        return ResponseEntity.ok("Super secret data for admins only.");
    }
}
```

#### **Kiểm tra theo Cấp bậc Tối thiểu (@RequiresProjectLevel)**

Dùng khi bạn muốn phân quyền theo cấp bậc (level), không quan tâm đến tên vai trò. Cách này rất hữu ích để phân cấp quản lý, ví dụ như "chỉ những người dùng có cấp bậc từ Trưởng phòng trở lên mới được xem báo cáo".

Thư viện sẽ tự động kiểm tra token Authorization: Bearer <end_user_token> trong header, gọi đến auth-service để lấy maxRoleLevel của người dùng và so sánh với giá trị bạn đặt trong annotation.

**Ví dụ:**
```java
import com.authplatform.client.security.RequiresProjectLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyManagementController {

    // API này yêu cầu EndUser phải có một vai trò với level từ 500 trở lên.
    // Owner có thể tạo role "MANAGER" (level 500) hoặc "DIRECTOR" (level 900),
    // và cả hai đều có thể truy cập API này.
    @GetMapping("/api/management/report")
    @RequiresProjectLevel(500)
    public ResponseEntity<String> getManagementReport() {
        return ResponseEntity.ok("This is a high-level management report.");
    }
}
```
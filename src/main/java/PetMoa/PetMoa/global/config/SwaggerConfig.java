package PetMoa.PetMoa.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * - API 문서 자동 생성
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI petMoaOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList());
    }

    private Info apiInfo() {
        return new Info()
                .title("PetMoa API")
                .description("""
                        # PetMoa - 동물병원 + 펫택시 통합 예약 플랫폼

                        ## 주요 기능
                        - 🏥 동물병원 예약 (수의사별, 진료 과목별)
                        - 🚕 펫택시 예약 (차량 크기별, 편도/왕복)
                        - 💰 통합 결제 (토스페이먼츠 연동)
                        - 📋 진료 기록 관리
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("munjji")
                        .email("ehowl1117@gmail.com")
                        .url("https://github.com/munjji/PETMOA_server"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                new Server()
                        .url("https://api.petmoa.com")
                        .description("운영 서버 (미정)")
        );
    }
}

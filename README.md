# MORU Backend

루틴 관리 앱 **MORU**의 백엔드 서버입니다.  
Spring Boot 기반으로 REST API, JWT 인증, JPA 기반 ORM, Swagger(OpenAPI) 문서화 등을 포함합니다.

---

## 🛠️ 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.3
- **Build Tool**: Gradle
- **Database**: MySQL 8.x + JPA (Hibernate)
- **Security**: Spring Security + JWT + BCrypt
- **API 문서화**: springdoc-openapi-ui (Swagger UI)
- **테스트 도구**: Swagger UI, Postman
- **배포 환경**: AWS EC2 + GitHub Actions (예정)

---

## 📁 프로젝트 구조

---

## 커밋 컨벤션

### 기본 형식
```
<type>: <message>
```

### type 목록
| 타입         | 설명                            |
| ---------- | ----------------------------- |
| `feat`     | 새로운 기능 추가                     |
| `fix`      | 버그 수정                         |
| `docs`     | 문서 수정 (README 등)              |
| `style`    | 코드 포맷팅, 세미콜론 누락 등 기능 변화 없는 수정 |
| `refactor` | 코드 리팩토링 (기능 변화 없음)            |
| `test`     | 테스트 코드 추가/수정                  |
| `chore`    | 빌드, 패키지 매니저 설정, 환경설정 등 기타 변경  |

### 예시
```
docs: 프로젝트 README 최초 작성
feat: 사용자 루틴 등록 API 추가
fix: 루틴 삭제 시 NullPointerException 해결
```
### Git Flow 개요
- `main`: 배포된 코드 (태그 관리)
- `develop`: 협업 기준 브랜치
- `feature/*`, `fix/*`, `refactor/*`: 작업별 브랜치
- `release/*`: 배포 전 안정화
- `hotfix/*`: 배포 후 긴급 수정

### ERD
<img width="4068" height="4424" alt="moru_db_ERD" src="https://github.com/user-attachments/assets/6a14744d-679a-408c-81c3-30e13a6215e8" />

### API 명세서
[link](https://15.164.150.204/swagger-ui/index.html#/)
https://denim-nectarine-c3b.notion.site/API-205843d01dfb816080e2c511cca67bd4?source=copy_link

### Architecture
<img width="771" height="1071" alt="architecture drawio" src="https://github.com/user-attachments/assets/a89463a8-febe-4b29-a419-cb7247565461" />



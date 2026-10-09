# 검증 기록

검증일: 2026-10-09 (Asia/Seoul)

## 환경

- Windows, 작업 폴더에만 설치한 Eclipse Temurin Java 21.0.12.1
- Apache Maven 3.9.11, Spring Boot 4.1.1
- 기본 시스템 Java 11과 시스템 환경변수는 변경하지 않음
- API 키 사용 없음, 모든 검증은 demo 또는 로컬 가짜 API 서버

## 자동 테스트

`mvn verify` 성공: 11 tests, 0 failures, 0 errors, 0 skipped.

최종 변경 후 `mvnw.cmd verify`로도 동일하게 통과했습니다. 이 PC의 Windows 다운로드 도구에 TLS 문제가 있어 공식 배포 ZIP을 내려받아 작업 폴더의 Wrapper 캐시에 준비한 후 실행했습니다. 다른 PC에서는 Wrapper가 Maven을 자동 다운로드합니다.

| 테스트 | 개수 | 확인 사항 |
| --- | ---: | --- |
| AiWorkAssistantApplicationTests | 1 | Spring 컨텍스트 구동 |
| ChatApiIntegrationTest | 5 | 데모 채팅, 빈 입력/길이 제한, system 역할/문맥 개수 제한, 잘못된 JSON 오류 정제, JSP 및 로컬 jQuery |
| OpenAiGatewayTest | 5 | 요청 헤더/본문/store:false, 다중 output_text, 429/401/500, 요청 타임아웃, 잘못된/빈/불완전 응답, 거절 |

OpenAI 테스트는 실제 네트워크 호출 구조를 로컬 HTTP 서버에 연결합니다. 외부 OpenAI 서비스나 실제 모델 답변 품질을 확인한 테스트는 아닙니다.

## 실행 검증

- 빌드한 WAR를 `java -jar`로 직접 실행
- `GET /api/status`: HTTP 200, demo
- JSP를 실제 Tomcat/Jasper로 렌더링, 소스 지시문이 HTML에 노출되지 않음
- 브라우저에서 회의록 카드 → 예시 입력 → 전송 → 데모 응답 표시 확인
- UI의 데모 배지와 ‘AI 생성 아님’ 안내 확인
- 업무 메일 예시 전송, 응답 복사 완료, 새 대화 초기화 확인
- 390px 모바일 너비에서 가로 넘침 없음 확인 (scrollWidth = clientWidth = 390)
- 브라우저 콘솔 오류/경고 없음
- `APP_AI_MODE=openai`, 키 미설정 시 명시적 오류로 시작 거부 확인
- JSP 세션 자동 생성 비활성화 및 보안 헤더 확인
- `.env`, `.env.production`, 로컬 설정, secrets, target 파일의 Git 제외 확인

## 검증 범위 밖

- 실제 API 키를 사용한 OpenAI 호출과 비용/계정 권한
- 외부 Tomcat 배포, 공개 인터넷 운영, 다중 사용자 부하
- DB 저장, 로그인/권한, 영속 대화 조회

GitHub Actions 설정은 포함했으나 원격 저장소에 올리지 않았으므로 원격 CI 실행 결과는 없습니다.

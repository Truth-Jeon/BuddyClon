# 프로젝트 소개
BUDDY CLON GV1은 MCNEX가 제작한 GPS 기반 골프 거리측정기입니다.
본 레포는 GV1 기기와 BLE(GATT)로 연동하여 기기 설정(언어/단위/볼륨/홀 안내)과 골프장 코스 맵 업데이트, 거리·코스 데이터 동기화를 제공하는 Android 네이티브 앱입니다.
- 의뢰처: MCNEX
- 역할: Android 클라이언트 단독 개발(리팩토링 포함)
- 범위: 기존 Java 코드를 Kotlin으로 리팩토링 + BLE 연동/네트워킹/화면 구성 정비
- 비고: 거리 계산은 기기(GV1)가 GPS+맵으로 수행, 앱은 설정/맵/동기화 담당

# 프로젝트 개발환경
- IDE : Android Studio
- 개발언어 : Kotlin (기존에 Java로 개발된 앱을 Kotlin으로 리팩토링 함)
- Framework : Android
  
# 프로그래머 정보
- 전하윤 :
  1. Kotlin 기반 리팩토링 및 모듈/레이어 정리
  2. (필요 화면) Jetpack Compose 컴포넌트 설계/적용
  3. MVVM 구조로 상태/로직 분리, 테스트·유지보수 용이성 개선
  4. Retrofit + Coroutines 네트워크 모듈 구현
  5. BLE(GATT) 스캔/페어링/데이터 교환 로직 및 예외 처리

# 네트워크 슈퍼마리오 브라더스

Java 소켓 프로그래밍을 활용한 멀티플레이어 슈퍼마리오 게임

## 프로젝트 개요

이 프로젝트는 기존 싱글플레이어 슈퍼마리오 게임을 확장하여 네트워크 멀티플레이어 기능을 추가한 것입니다. Java Socket Programming (TCP)을 사용하여 최대 4명의 플레이어가 동시에 게임을 플레이할 수 있습니다.

### 주요 기능

- **싱글플레이어 모드**: 기존 게임 기능 완전 유지
- **멀티플레이어 모드**: 최대 4명 동시 플레이 지원
- **서버-클라이언트 아키텍처**: TCP 기반 신뢰성 있는 통신
- **실시간 상태 동기화**: 플레이어 위치, 적, 아이템, 점수 등
- **게임 로비 시스템**: 서버 대기 화면 및 접속자 관리
- **독립적 카메라**: 각 클라이언트가 자신의 플레이어를 추적

## 기술 스택

- **언어**: Java
- **GUI**: Java Swing
- **네트워킹**: Java Socket Programming (TCP)
- **프로토콜**: 커스텀 메시지 기반 프로토콜

## 프로젝트 구조

```
Super-Mario-Bros/
├── src/
│   ├── manager/          # 게임 로직 관리
│   │   ├── GameEngine.java
│   │   ├── MapManager.java
│   │   ├── GameStatus.java
│   │   └── ...
│   ├── model/           # 게임 오브젝트
│   │   ├── hero/        # 마리오, 파이어볼
│   │   ├── enemy/       # 적 캐릭터
│   │   ├── brick/       # 블록과 파이프
│   │   └── prize/       # 아이템
│   ├── view/            # UI 및 렌더링
│   │   ├── UIManager.java
│   │   └── ...
│   ├── network/         # 네트워크 레이어
│   │   ├── NetworkManager.java
│   │   ├── server/      # 서버 구현
│   │   ├── client/      # 클라이언트 구현
│   │   └── protocol/    # 프로토콜 정의
│   └── ranking/         # 랭킹 시스템
├── .claude/             # 개발 문서
├── compile.bat          # 컴파일 스크립트
├── run_game.bat         # 게임 실행 스크립트
├── run_server.bat       # 서버 실행 스크립트
├── run_client.bat       # 클라이언트 실행 스크립트
├── TEST_GUIDE.md        # 테스트 가이드
└── README.md            # 이 파일
```

## 빠른 시작

### 1. 컴파일

프로젝트 루트 디렉토리에서 다음 명령을 실행하거나 `compile.bat`을 실행합니다.

```powershell
javac -encoding UTF-8 -d . -sourcepath src src/manager/GameEngine.java
```

### 2. 실행

#### 싱글플레이어 모드

```powershell
java -cp . manager.GameEngine
```

또는 `run_game.bat`을 실행하고 "게임 시작"을 선택합니다.

#### 멀티플레이어 모드

**서버 (호스트)**:
```powershell
java -cp . manager.GameEngine
```
시작 화면에서 "서버 시작"을 선택합니다.

**클라이언트 (게스트)**:
```powershell
java -cp . manager.GameEngine
```
시작 화면에서 "서버 접속"을 선택하고 스페이스바를 눌러 연결합니다.

## 게임 조작법

- **방향키 ←/→**: 이동
- **스페이스바**: 점프
- **엔터**: 선택/확인
- **ESC**: 일시정지

## 네트워크 아키텍처

### 서버-클라이언트 모델

- **서버**: 게임 상태 권한, 물리 시뮬레이션, 충돌 감지
- **클라이언트**: 렌더링, 입력 처리, 상태 동기화

### 통신 프로토콜

- **전송 방식**: TCP (신뢰성 있는 상태 동기화)
- **메시지 포맷**: 커스텀 프로토콜 (Java 직렬화)
- **동기화 주기**: 20-30 Hz (초당 20-30회)

### 메시지 타입

- `CONNECT`: 클라이언트 연결 요청
- `CONNECTED`: 서버 연결 승인
- `INPUT`: 클라이언트 입력 전송
- `GAME_STATE`: 게임 상태 브로드캐스트
- `PLAYER_JOINED`: 플레이어 참가 알림
- `PLAYER_LEFT`: 플레이어 퇴장 알림
- `GAME_START`: 게임 시작 신호
- `DISCONNECT`: 연결 종료

## 개발 문서

자세한 개발 계획 및 아키텍처 설계는 `.claude/` 디렉토리의 문서를 참조하세요:

- `CLAUDE.md`: 전체 개발 계획
- `NETWORK_ARCHITECTURE.md`: 네트워크 아키텍처 상세 설계
- `IMPLEMENTATION_PLAN.md`: 최소 수정 구현 전략
- `GAME_DESIGN.md`: 게임 모드 및 멀티플레이어 규칙
- `PHASE4_SUMMARY.md`: Phase 4 완료 보고서
- `TEST_GUIDE.md`: 테스트 시나리오 및 가이드

## 테스트

자세한 테스트 가이드는 `TEST_GUIDE.md`를 참조하세요.

### 로컬 멀티플레이어 테스트

1. 터미널 1: `run_game.bat` 실행 → "서버 시작" 선택
2. 터미널 2: `run_game.bat` 실행 → "서버 접속" 선택
3. 서버 화면에서 스페이스바로 게임 시작
4. 양쪽에서 게임 플레이

## 알려진 제한사항

- 클라이언트 서버 주소가 현재 localhost로 하드코딩됨
- 플레이어 ID가 동적으로 할당되지 않음
- 재연결 기능 미구현

## 향후 개선 계획

- [ ] 동적 플레이어 ID 할당
- [ ] UI에서 서버 주소 입력 기능
- [ ] 클라이언트 측 예측 및 지연 보상
- [ ] 재연결 기능
- [ ] 채팅 시스템
- [ ] 게임 내 플레이어 커스터마이징

## 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.

원본 게임: https://github.com/ahmetcandiroglu/Super-Mario-Bros

## 기여자

- 네트워크프로그래밍 팀
- 한성대학교 (HSU)

## 문의

프로젝트 관련 문의사항은 이슈를 등록해 주세요.

---

**개발 기간**: 2024-12-14
**버전**: 1.0
**상태**: Phase 4 완료 - 통합 테스트 준비

# 네트워크 프로그래밍 수업 자료 요약

> 이 문서는 수업 PDF 자료의 핵심 내용과 주요 코드를 정리한 참고 자료입니다.
> 프로젝트 개발 시 이 패턴들을 기본으로 활용합니다.

---

## 1. 자바 기반 소켓 프로그래밍 기초

### 핵심 개념
- **TCP/IP**: TCP는 신뢰성 있는 데이터 전송, IP는 패킷 교환 담당
- **IP 주소와 포트**: IP는 컴퓨터 식별, 포트는 응용프로그램 식별
- **소켓(Socket)**: 네트워크 통신의 끝단
  - 서버 소켓: 연결 대기
  - 클라이언트 소켓: 연결 요청

### 기본 코드 패턴

**클라이언트 소켓**
```java
Socket clientSocket = new Socket("128.12.1.1", 5550); // 접속 요청
```

**서버 소켓**
```java
ServerSocket serverSocket = new ServerSocket(5550);
Socket socket = serverSocket.accept(); // 연결 대기 (Blocking)
```

---

## 2. IP와 포트의 이해

### 핵심 개념
- **포트의 필수성**: TCP/IP 프로그램은 반드시 포트 번호 사용
- **Localhost**: `127.0.0.1` 또는 `localhost`
  - 자신의 컴퓨터를 가리키는 주소
  - 한 PC에서 클라이언트-서버 테스트 시 사용

---

## 3. 소켓 프로그래밍 상세

### 연결 과정 (3-way handshake)
1. 클라이언트: `new Socket()` 호출
2. 내부적으로 SYN → SYN+ACK → ACK 과정
3. 연결 수립 완료

### 소켓 객체 구성
- 상대방 주소 정보
- 입출력 스트림: `InputStream`, `OutputStream`

### 인코딩 처리
- 한글 깨짐 방지: `UTF-8` 또는 `CP949` 사용

### 핵심 코드 패턴
```java
// 입출력 스트림을 버퍼 스트림으로 감싸기 (성능 향상)
BufferedReader in = new BufferedReader(
    new InputStreamReader(socket.getInputStream())
);
BufferedWriter out = new BufferedWriter(
    new OutputStreamWriter(socket.getOutputStream())
);
```

---

## 4. 입출력 스트림 정리

### 주요 스트림 클래스

**InputStream/OutputStream**
- 바이트 단위 데이터 처리
- 기본 스트림

**InputStreamReader/OutputStreamWriter**
- 바이트 스트림 → 문자 스트림 변환
- 보조 스트림

**BufferedReader/BufferedWriter**
- 버퍼 사용으로 속도 향상
- `readLine()`: 줄 단위 입력 가능

**DataInputStream/DataOutputStream**
- 기본 자료형(int, float 등) 전송에 유용

### 핵심 코드 패턴
```java
// DataStream 활용
DataInputStream in = new DataInputStream(socket.getInputStream());
int num = in.readInt(); // 정수 읽기
String str = in.readUTF(); // 문자열 읽기

DataOutputStream out = new DataOutputStream(socket.getOutputStream());
out.writeInt(123); // 정수 쓰기
out.writeUTF("Hello"); // 문자열 쓰기
```

---

## 5. 스레드(Thread)

### 핵심 개념
- **스레드**: JVM이 스케줄링하는 실행 단위
- **멀티태스킹**: 하나의 프로그램에서 여러 작업 동시 처리
- **필수 사용 사례**: 네트워크 송수신 병렬 처리

### 구현 방법

**방법 1: Thread 클래스 상속**
```java
class MyThread extends Thread {
    public void run() {
        // 작업 내용
    }
}

// 사용
new MyThread().start();
```

**방법 2: Runnable 인터페이스 구현 (권장)**
```java
class MyRunnable implements Runnable {
    public void run() {
        // 작업 내용
    }
}

// 사용
new Thread(new MyRunnable()).start();
```

---

## 6. 자바 GUI - Swing 기초

### 핵심 개념
- **컨테이너**: `JFrame` 등 - 컴포넌트를 담는 그릇
- **컴포넌트**: `JButton`, `JLabel` 등 - 실제 UI 요소
- **ContentPane**: 컴포넌트 부착 위치

### 기본 코드 패턴
```java
JFrame f = new JFrame("Title");
f.setLayout(new FlowLayout());
f.add(new JButton("Click"));
f.setSize(300, 200);
f.setVisible(true);
f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

---

## 7. Swing 배치 관리자 (Layout Manager)

### 주요 레이아웃

**FlowLayout**
- 왼쪽→오른쪽 순서대로 배치

**BorderLayout**
- 동, 서, 남, 북, 중앙 5개 영역

**GridLayout**
- 격자(행, 열) 모양 배치

**절대 위치 배치**
```java
container.setLayout(null); // 배치관리자 제거
component.setLocation(x, y);
component.setSize(width, height);
```

---

## 8. Swing 주요 컴포넌트

- `JLabel`: 텍스트/이미지 표시
- `JButton`: 버튼
- `JTextField`: 한 줄 텍스트 입력
- `JTextArea`: 여러 줄 텍스트 입력
- `JCheckBox`: 체크박스
- `JRadioButton`: 라디오 버튼
- `JList`: 리스트
- `JComboBox`: 드롭다운 메뉴
- `JMenu`: 메뉴바
- `JOptionPane`: 팝업 다이얼로그

---

## 9. 이벤트 처리

### 이벤트 기반 프로그래밍 구조
1. 이벤트 발생 (예: 버튼 클릭)
2. 이벤트 객체 생성
3. 리스너 실행

### 리스너 구현 방법
1. 독립 클래스
2. 내부 클래스
3. 익명 클래스 (가장 많이 사용)
4. 현재 클래스에 직접 구현

### 핵심 코드 패턴 - 익명 클래스
```java
button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        // 버튼 클릭 시 실행될 코드
    }
});
```

### 주요 리스너 인터페이스
- `ActionListener`: 버튼 클릭, 엔터 키 등
- `MouseListener`: 마우스 이벤트
- `KeyListener`: 키보드 이벤트
- `WindowListener`: 창 이벤트

---

## 10. 서버-클라이언트 채팅 (GUI) 패턴

### 전형적인 구조
```java
// 서버
public class ChatServer extends JFrame {
    ServerSocket serverSocket;
    Socket socket;
    BufferedReader in;
    BufferedWriter out;

    public void startServer() {
        // 1. 서버 소켓 생성
        serverSocket = new ServerSocket(port);

        // 2. 클라이언트 연결 대기
        socket = serverSocket.accept();

        // 3. 스트림 생성
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // 4. 수신 스레드 시작
        new Thread(new Receiver()).start();
    }

    class Receiver implements Runnable {
        public void run() {
            while (true) {
                String msg = in.readLine();
                // GUI에 메시지 표시
            }
        }
    }
}
```

---

## 11. 다중 클라이언트 채팅 패턴

### 핵심 개념
- **클라이언트 목록 관리**: `Vector<Socket>` 또는 `ArrayList<Socket>`
- **브로드캐스팅**: 모든 클라이언트에게 메시지 전송
- **각 클라이언트별 스레드**: 동시에 여러 클라이언트 처리

### 핵심 코드 패턴
```java
public class MultiChatServer {
    Vector<ClientHandler> clients = new Vector<>();

    public void acceptClients() {
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            handler.start();
        }
    }

    class ClientHandler extends Thread {
        Socket socket;
        BufferedReader in;
        BufferedWriter out;

        public void run() {
            while (true) {
                String msg = in.readLine();
                broadcast(msg); // 모든 클라이언트에게 전송
            }
        }
    }

    void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.out.write(msg + "\n");
            client.out.flush();
        }
    }
}
```

---

## 12. UDP 소켓 프로그래밍

### TCP vs UDP
- **TCP**: 연결 지향, 신뢰성, 순서 보장
- **UDP**: 비연결성, 빠름, 신뢰성 낮음

### UDP 코드 패턴
```java
// 수신
DatagramSocket socket = new DatagramSocket(port);
byte[] buffer = new byte[1024];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet); // 데이터 수신
String msg = new String(packet.getData(), 0, packet.getLength());

// 송신
String msg = "Hello";
byte[] data = msg.getBytes();
InetAddress address = InetAddress.getByName("127.0.0.1");
DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
socket.send(packet);
```

---

## 종합 패턴: 네트워크 게임 개발 핵심 구조

### 기본 아키텍처
```
서버:
1. ServerSocket으로 연결 대기
2. 각 클라이언트별 Socket + Thread 생성
3. 게임 상태 관리 및 브로드캐스팅
4. DataInputStream/DataOutputStream으로 게임 데이터 송수신

클라이언트:
1. Socket으로 서버 연결
2. 수신 Thread: 게임 상태 업데이트 받기
3. 송신 Thread: 입력 이벤트 전송
4. Swing GUI: 게임 화면 렌더링
5. KeyListener: 키 입력 처리
```

### 핵심 통신 패턴
```java
// 서버: 게임 상태 전송
DataOutputStream out = new DataOutputStream(socket.getOutputStream());
out.writeInt(playerX);
out.writeInt(playerY);
out.writeUTF(playerState);
out.flush();

// 클라이언트: 게임 상태 수신
DataInputStream in = new DataInputStream(socket.getInputStream());
int x = in.readInt();
int y = in.readInt();
String state = in.readUTF();
// GUI 업데이트
```

---

## 프로젝트 적용 전략

### 1단계: 기본 연결
- ServerSocket + Socket 구현
- localhost 테스트
- 연결 확인 메시지

### 2단계: 데이터 통신
- DataInputStream/DataOutputStream 사용
- 간단한 데이터 송수신 테스트

### 3단계: 멀티스레드
- 수신 스레드 구현
- GUI 업데이트와 분리

### 4단계: 게임 통합
- 기존 게임 엔진과 네트워크 레이어 연결
- 입력 → 전송 → 수신 → 렌더링 파이프라인 구축

### 5단계: 다중 클라이언트
- Vector로 클라이언트 관리
- 브로드캐스팅 구현

---

**참고**: 이 패턴들은 수업 자료에서 검증된 방법이므로, 프로젝트에서 이를 기본으로 활용합니다.

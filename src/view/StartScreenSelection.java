package view;

// 시작 화면 메뉴 선택 항목
public enum StartScreenSelection {
    CREATE_ROOM(0),     // 방 생성
    JOIN_ROOM(1),       // 방 참가
    VIEW_HELP(2),       // 도움말
    VIEW_ABOUT(3);      // 정보

    private final int lineNumber;
    
    StartScreenSelection(int lineNumber){ 
        this.lineNumber = lineNumber; 
    }

    public StartScreenSelection getSelection(int number){
        switch(number) {
            case 0: return CREATE_ROOM;
            case 1: return JOIN_ROOM;
            case 2: return VIEW_HELP;
            case 3: return VIEW_ABOUT;
            default: return null;
        }
    }

    // 메뉴 항목 위/아래 이동 (순환)
    public StartScreenSelection select(boolean toUp){
        int selection;

        if(lineNumber > -1 && lineNumber < 4){
            selection = lineNumber - (toUp ? 1 : -1);
            if(selection == -1)
                selection = 3;
            else if(selection == 4)
                selection = 0;
            return getSelection(selection);
        }

        return null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}

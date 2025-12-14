package view;

public enum StartScreenSelection {
    START_GAME(0),
    CREATE_ROOM(1),
    JOIN_ROOM(2),
    VIEW_HELP(3),
    VIEW_ABOUT(4);

    private final int lineNumber;
    StartScreenSelection(int lineNumber){ this.lineNumber = lineNumber; }

    public StartScreenSelection getSelection(int number){
        switch(number) {
            case 0: return START_GAME;
            case 1: return CREATE_ROOM;
            case 2: return JOIN_ROOM;
            case 3: return VIEW_HELP;
            case 4: return VIEW_ABOUT;
            default: return null;
        }
    }

    public StartScreenSelection select(boolean toUp){
        int selection;

        if(lineNumber > -1 && lineNumber < 5){
            selection = lineNumber - (toUp ? 1 : -1);
            if(selection == -1)
                selection = 4;
            else if(selection == 5)
                selection = 0;
            return getSelection(selection);
        }

        return null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}

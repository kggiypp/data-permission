package com.github.datapermission.banner;

import org.springframework.boot.ansi.AnsiOutput;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.boot.ansi.AnsiColor.CYAN;
import static org.springframework.boot.ansi.AnsiColor.GREEN;
import static org.springframework.boot.ansi.AnsiOutput.Enabled;
import static org.springframework.boot.ansi.AnsiOutput.Enabled.ALWAYS;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

/**
 * 数据权限插件专属banner
 * 
 * @author keguang
 * @date 2024/4/25 14:23
 */
public class DataPermissionBanner {
    
    private static final String BANNER = "\n" +
            "     _                                                                                   \n" +
            "    | |          _                                       _              _                \n" +
            "  __| |  ____  _| |_   ____  ____    ____   _ __  _____ (_) ____  ____ (_)   _    ____   \n" +
            " / _  | / _  ||_   _| / _  ||  _ \\  / _  \\ | '__|| _ _ || |/ _ _|/ _ _|| | / _ \\ | __ |  \n" +
            "( |_| |( |_| |  | |_ ( |_| || |_| )( (_ _/ | |   | | | || |\\_ _ \\\\_ _ \\| |( (_) )| || |  \n" +
            " \\____| \\__'_|  |___| \\__'_||  __/  \\____\\ |_|   |_|_|_||_||____/|____/|_| \\ _ / |_||_|  \n" +
            "                            | |                                                          \n" +
            "                            |_|                                                          ";
    
    private static final String DATA_PERMISSION = ":: Data Permission :: ";
    
    private static final String REMIND_ENABLED = "plguin is enabled ";

    private static final String NORMAL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    private static final String GOOD_LUCK = "Good luck!";
    
    public static void printBanner(PrintStream printStream) {
        printStream.println(BANNER);
        // 借用springboot特效工具炫技
        Enabled previous = AnsiOutput.getEnabled();
        AnsiOutput.setEnabled(ALWAYS);
        printStream.println(AnsiOutput.toString(CYAN, DATA_PERMISSION, GREEN, BOLD, REMIND_ENABLED, NORMAL, "at ",
                new SimpleDateFormat(NORMAL_DATETIME_PATTERN).format(new Date()), ". ", GOOD_LUCK));
        AnsiOutput.setEnabled(previous);
    }
    
}

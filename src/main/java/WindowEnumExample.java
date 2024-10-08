import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.sun.jna.platform.win32.User32.HWND;

public class WindowEnumExample {

    private static final BufferedWriter FW;

    private static final BufferedWriter LOG;

    static {
        try {
            FW = new BufferedWriter(new FileWriter("C:\\Users\\Administrator\\Desktop\\subWindow.txt", true));
            LOG = new BufferedWriter(new FileWriter("C:\\Users\\Administrator\\Desktop\\AutoClose.log", true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static User32 INSTANCE = Native.load("user32", User32.class);

    static class Tuple {
        public Tuple(WinDef.HWND hWnd, Pointer pointer) {
            this.hWnd = hWnd;
            this.pointer = pointer;
        }
        WinDef.HWND hWnd;
        Pointer pointer;

        public WinDef.HWND gethWnd() {
            return hWnd;
        }
    }


    public interface User32 extends StdCallLibrary {

        interface WindowCallback extends StdCallCallback {
            boolean callback(WinDef.HWND hWnd, Pointer arg);
        }

        // 定义一个枚举子窗口的回调函数
        interface EnumWindowsProc extends StdCallCallback {
            boolean callback(WinDef.HWND hWnd, Pointer refData);
        }

        boolean EnumWindows(WindowCallback lpEnumFunc, Pointer arg);

        boolean EnumChildWindows(WinDef.HWND hWndParent, EnumWindowsProc lpEnumFunc, Pointer refData);

        // 获取窗口标题
        int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);

        // 窗口前置
        boolean SetForegroundWindow(WinDef.HWND hWnd);

        int GetClassNameA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);

        default String GetClassNameA(WinDef.HWND hWnd) {
            byte[] title = new byte[512];
            GetClassNameA(hWnd, title, title.length);
            return Native.toString(title);
        }

        default String GetWindowTextW(WinDef.HWND hWnd) {
            char[] title = new char[512];
            GetWindowTextW(hWnd, title, title.length);
            return Native.toString(title);
        }

        WinDef.LRESULT SendMessageA(WinDef.HWND hWnd, int msg, WinDef.WPARAM wparam, WinDef.LPARAM lparam);

    }


    public interface WindowEnumCallback extends User32.WindowCallback {
        boolean callback(WinDef.HWND hWnd, Pointer arg);
    }


    private static HWND enumAllSubWindows(WinDef.HWND root, int indent, Consumer<String> logger) throws IOException {

//        if(INSTANCE.GetClassNameA(root).equalsIgnoreCase("Edit")) {
//            return root;
//        }

        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("        ");
        }

        logger.accept(indentStr + INSTANCE.GetClassNameA(root) + " -- " + INSTANCE.GetWindowTextW(root));

        List<WinDef.HWND> subHandlers = new ArrayList<>();

        INSTANCE.EnumChildWindows(root, new User32.EnumWindowsProc() {
            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer refData) {
                subHandlers.add(hWnd);
                return true; // 继续枚举
            }
        }, null);

        if(subHandlers.isEmpty()) {
            return null;
        }

        for (WinDef.HWND subHandler : subHandlers) {
            HWND hwnd = enumAllSubWindows(subHandler, indent + 1, logger);
            if(hwnd != null) {
                return hwnd;
            }
        }

        return null;
    }

    public static void main(String[] args) throws Exception {

        while(true) {

            final List<HWND> WARNING_WINDOWS = new ArrayList<>();
            INSTANCE.EnumWindows((WindowEnumCallback) (hWnd, pointer) -> {
                String title = INSTANCE.GetWindowTextW(hWnd);
//            if(title.equals("ABC.txt - 记事本")) {
                if(title.equals("Microsoft Word 安全声明")) {
                    WARNING_WINDOWS.add(hWnd);
                }
                return true;
            }, null);

            for (HWND warningWindow : WARNING_WINDOWS) {
//                System.out.println("窗口前置：" + INSTANCE.SetForegroundWindow(warningWindow));
                LOG.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
                LOG.newLine();
                LOG.write("ClassName：" + INSTANCE.GetClassNameA(warningWindow));
                LOG.newLine();

                enumAllSubWindows(warningWindow, 0, a -> {
                    try {
                        LOG.write(a);
                        LOG.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                // 直接关闭窗口，没选中"是(Y)"，应该发送一个Y按键消息
                LOG.write("SendCloseMessage: " + INSTANCE.SendMessageA(warningWindow, com.sun.jna.platform.win32.User32.WM_CLOSE, new WinDef.WPARAM(), new WinDef.LPARAM()));
                LOG.newLine();
                LOG.flush();
            }

            Thread.sleep(3000L);
        }
    }


    public static void main2(String[] args) throws IOException {

        final List<Tuple> windowHandles = new ArrayList<>();
        INSTANCE.EnumWindows((WindowEnumCallback) (hWnd, pointer) -> {
            String title = INSTANCE.GetWindowTextW(hWnd);
            System.out.println(title);
//            if(title.equals("ABC.txt - 记事本")) {
            if(title.equals("Microsoft Word 安全声明")) {
                windowHandles.add(new Tuple(hWnd, pointer));
            }
            return true;
        }, null);

        if(!windowHandles.isEmpty()) {
            Tuple tuple = windowHandles.get(0);
            System.out.println("窗口前置：" + INSTANCE.SetForegroundWindow(tuple.gethWnd()));
            System.out.println("ClassName：" + INSTANCE.GetClassNameA(tuple.gethWnd()));

            // 直接关闭窗口，没选中"是(Y)"，应该发送一个Y按键消息
            System.out.println("SendCloseMessage: " + INSTANCE.SendMessageA(tuple.gethWnd(), com.sun.jna.platform.win32.User32.WM_CLOSE, new WinDef.WPARAM(), new WinDef.LPARAM()));;

            // N按下去、松开
//            System.out.println("SendKeyDownMessage: " + INSTANCE.SendMessageA(tuple.gethWnd(), WM_KEYDOWN, new WinDef.WPARAM(Win32VK.VK_N.code), new WinDef.LPARAM(Win32VK.VK_N.code)));
//            System.out.println("SendKeyUpMessage: " + INSTANCE.SendMessageA(tuple.gethWnd(), WM_KEYUP, new WinDef.WPARAM(Win32VK.VK_N.code), new WinDef.LPARAM(Win32VK.VK_N.code)));


//            HWND editWindow = enumAllSubWindows(tuple.gethWnd(), 0);
//
//            if(editWindow != null) {
//                System.out.println("SendKeyDownMessage: " + INSTANCE.SendMessageA(editWindow, WM_KEYDOWN, new WinDef.WPARAM(Win32VK.VK_N.code), new WinDef.LPARAM(Win32VK.VK_N.code)));
//                System.out.println("SendKeyUpMessage: " + INSTANCE.SendMessageA(editWindow, WM_KEYUP, new WinDef.WPARAM(Win32VK.VK_N.code), new WinDef.LPARAM(Win32VK.VK_N.code)));
//                System.out.println("SendCharMessage: " + INSTANCE.SendMessageA(editWindow, WM_CHAR, new WinDef.WPARAM('K'), new WinDef.LPARAM(0)));
//            }

//            INSTANCE.EnumChildWindows(tuple.gethWnd(), new User32.EnumWindowsProc() {
//                @Override
//                public boolean callback(WinDef.HWND hWnd, Pointer refData) {
//                    // 这里可以根据按钮的类名或者标题来筛选按钮
//                    String classNameA = INSTANCE.GetClassNameA(hWnd);
//                    try {
//                        FW.write(classNameA + " -- " + INSTANCE.GetWindowTextW(hWnd));
//                        FW.newLine();
//                        FW.flush();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return true; // 继续枚举
//                }
//            }, null);

        }

        FW.flush();
    }
}

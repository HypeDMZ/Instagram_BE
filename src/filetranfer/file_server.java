package filetranfer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class file_server {


    private static class ConnectThread extends Thread
    {
        ServerSocket serverSocket;
        int count = 1;

        ConnectThread (ServerSocket serverSocket) //생성자를 통해 서버소켓을 받음
        {
            System.out.println(" Server opened"); //서버가 열렸다는 메세지 출력
            this.serverSocket = serverSocket; //서버소켓을 저장
        }

        @Override
        public void run ()
        {
            try
            {
                while (true) //계속 새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                {
                    Socket socket = serverSocket.accept();  //클라이언트의 연결을 수락
                    System.out.println("    Thread " + count + " is started.");
                    file_server_multithread serverThread = new file_server_multithread(socket, count);
                    serverThread.start(); //새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                    count++;
                }
            } catch (IOException e)
            {
                System.out.println(e);
                System.out.println("    SERVER CLOSE    ");
            }
        }
    }

    public static void main(String[] args){
        ServerSocket serverSocket = null;
        try
        {   // 서버소켓을 생성, 8080 포트와 binding
            serverSocket = new ServerSocket(8080); // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket); // 서버소켓을 connectThread에 넘겨줌
            connectThread.start(); // connectThread 시작


        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }




    private static class file_server_multithread extends Thread
    {
        Socket socket;
        int id;

        String filesavepath="C:\\Users\\Administrator\\IdeaProjects\\instagram_server\\src\\filetranfer\\";

        FileOutputStream fileOutput = null;
        DataInputStream dataInput = null;

        InputStream os =null;
        byte[] buf = null;
        BufferedInputStream bufferdInput = null; //input 속도 향상을 위한 BufferedInputStream

        //생성자를 통해 입력받은 소켓과 클라이언트(쓰레드)의 id를 저장
        file_server_multithread (Socket socket, int id)
        {
            this.socket = socket;
            this.id = id;
        }

        public String getServerDateTime(){
            String DateTime=null;
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
            DateTime = now.format(formatter);
            return DateTime;

        }



        @Override
        public void run ()
        {
            try {
                os=socket.getInputStream();
                dataInput = new DataInputStream(os); //송신측에서 연결요청시 accept

                // 메소드에서 송신측과
                // 연결을 위한 연결소켓생성
                int totalSize = dataInput.readInt();       //전송받을 파일 사이즈 수신및 변수에 저장
                System.out.println(totalSize);  //수신 파일 사이즈 콘솔출력
                String filetype=null; //파일 확장자 변수
                byte[] filetype_inbyte= new byte[20];
                os.read(filetype_inbyte); //파일 타입 송신자로부터 수신
                filetype=new String(filetype_inbyte);
                filetype=filetype.trim();
                System.out.println("file type:"+filetype);
                String fileName = getServerDateTime()+filetype;
                buf = new byte[104857600];      //100MB 단위로 파일을 쓰기 위한 byte타입 배열
                fileOutput = new FileOutputStream(filesavepath+fileName, false);
                bufferdInput = new BufferedInputStream(dataInput);
                int i = 0;  //buf 배열 인덱스용 변수

                //전송받은 파일 사이즈가 100MB 보다 크다면 100MB 단위로 배열에 저장 후 파일에 write 하고
                //파일에 write한 100MB만큼을  파일 사이즈에서 제외하는 while문!!!
                while (totalSize > 104857600) {
                    while (i < 104857600) {
                        buf[i] = (byte) bufferdInput.read();
                        i++;    //배열인덱스 이동
                    }//while(i < 104857600)문
                    totalSize -= 104857600;  //파일사이즈 - 100MB
                    i = 0;                   //배열 인덱스 초기화
                    fileOutput.write(buf);   //파일에 write
                }//while(totalSize > 104857600)문

                //100MB보다 같거나 작은 남은 사이즈 혹은 원래의 사이즈가 100MB 보다 작을 시 if문 내용이 실행 되어
                //파일을 write 함
                if (totalSize <= 104857600) {
                    i = 0;                     //배열 인덱스 초기화
                    buf = new byte[totalSize]; //100MB보다 같거나 작으므로 totalSize로 배열크기 다시 생성
                    while (i < totalSize) {
                        buf[i] = (byte) bufferdInput.read();
                        i++;      //배열인덱스 이동
                    }//while문
                    fileOutput.write(buf);  //파일에 write
                }//if문
                fileOutput.flush();
                System.out.println("file receive complete");
            } catch (IOException e) {
                System.out.println("error occurred at file save phase");
                e.printStackTrace();
            } finally {
                try {
                    if (bufferdInput != null)
                        bufferdInput.close();
                    if (dataInput != null)
                        dataInput.close();
                    if (fileOutput != null)
                        fileOutput.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }//finally
        }
    }
}


//ClientListening 클래스
import java.awt.*;
//import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;

class Panel extends JPanel{
    private BufferedImage image;
    private CascadeClassifier eye_cascade=new CascadeClassifier("C:/opencv/sources/data/haarcascades/haarcascade_mcs_eyepair_big.xml");
    private Byte flag=0; private int xaxis=683,yaxis=384,xblink,yblink;
    public Mat detect(Mat frame){
        MatOfRect eyepair=new MatOfRect();
        xblink=0; yblink=0;
        eye_cascade.detectMultiScale(frame,eyepair);
        Mat crop=new Mat(); Rect rectCrop=null;
        for (Rect rect:eyepair.toArray()) rectCrop=new Rect(rect.x,rect.y,rect.width,rect.height);
        if(rectCrop!=null){
            crop=new Mat(frame,rectCrop);
            Core.flip(crop,crop,1);
            Imgproc.resize(crop,crop,new Size(600,150));
            Imgproc.equalizeHist(crop,crop);
            Imgproc.threshold(crop,frame,15,255,Imgproc.THRESH_BINARY);
            List<MatOfPoint>contours=new ArrayList<>();
            Imgproc.findContours(frame,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            for(int i=0;i<contours.size();i++){
                if (Imgproc.contourArea(contours.get(i))>2000&&Imgproc.contourArea(contours.get(i))<4000){
                    Rect eye=Imgproc.boundingRect(contours.get(i));
                    if (eye.height>30)
                        Core.rectangle(crop,new Point(eye.x,eye.y),new Point(eye.x+eye.width,eye.y+eye.height),new Scalar(255,255,255));
                    xblink+=eye.x+(eye.width/2); yblink+=eye.y+(eye.height/2);
                }
            }
        }
        return crop;
    }
    public void mouse(){
        if(xblink>500){
            xaxis=(683+9*xaxis+(xblink-600)*30)/10;
            yaxis=(384+9*yaxis+(yblink-110)*45)/10;
            System.out.println("x= "+xaxis+"    y= "+yaxis);}
        try{
            Robot robot=new Robot();
            if(xblink>500)
                robot.mouseMove(xaxis,yaxis);
            /*else if(xblink<250&&xblink>0)
                    robot.mousePress(InputEvent.BUTTON2_MASK);
                else if(xblink<550&&xblink>0)
                    robot.mousePress(InputEvent.BUTTON1_MASK);*/
        }catch(Exception e){}
    }
    public void MatToBufferedImage(Mat cam){
        if(cam.empty())flag=2; else flag=1;
        byte[] sourcePixels=new byte[66000];
        cam.get(0,0,sourcePixels);
        image=new BufferedImage(600,110,BufferedImage.TYPE_BYTE_GRAY);
        byte[] targetPixels=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels,0,targetPixels,0,66000);
    }
    public void paintComponent(Graphics g){
        g.drawImage(this.image,0,0,600,110,null);
        g.setColor(Color.red);
        g.setFont(new Font("TimesRoman",Font.BOLD,20));
if(flag!=1){
            if (flag==2) g.drawString("No Eyes Detected!",200,60);
            else g.drawString("Initializing Camera...",200,60);}
        else{
            if(xblink<200&&xblink>0) g.drawString("Right Click",250,100);
            else if(xblink<500&&xblink>0) g.drawString("Left Click",250,100);}
    }
}
public class Project{
    public static void main(String arg[]){
    System.loadLibrary("opencv_java2411");
    JFrame frame=new JFrame("Cursor Control using Eye Movement");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Panel panel=new Panel();
    frame.setContentPane(panel);
    frame.setSize(600,150);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    Mat webcam=new Mat();
    VideoCapture capture=new VideoCapture(1);
    while(true){
        capture.read(webcam);
        if(!webcam.empty()){
            Imgproc.cvtColor(webcam,webcam,Imgproc.COLOR_BGR2GRAY);
            webcam=panel.detect(webcam);
            panel.mouse();
            panel.MatToBufferedImage(webcam);
            panel.repaint();}}
    }
}

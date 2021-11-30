package jp.ac.nihon_u.cit.su.furulab.fuse.util;

/** 論理時刻変換。日の概念はいらないので時間だけ */
public class TimeConverter {
    private static long startTime;

    /** 時刻をオフセットします */
    public static void setStartTime(int hours ,int minuts, int seconds){
        startTime=(hours*3600+minuts*60+seconds)*1000;
    }

    /** 時刻を時分秒で表した文字列として取得します<br>
     * get time as hh:mm'ss format */
    public static String getTimeAsString(long millis){
        int[] timeArray=getTimeAsIntArray(millis+startTime);

        String timeStr=toTwoChrs(timeArray[0])+":"+toTwoChrs(timeArray[1])+"'"+toTwoChrs(timeArray[2]);
        return timeStr;
    }

    public static String toTwoChrs(int val){
        String str=String.valueOf(val);
        if (str.length()==1){
            str="0"+str;
        }

        return str;
    }

    /** 時刻を時分秒で取得します */
    public static int[] getTimeAsIntArray(long millis){
        int[] hourMinutSecond=new int[3];
        long timeSecond=millis/1000; // 秒単位に変更

        int hour=(int)(timeSecond/3600);
        timeSecond-=hour*3600;
        int minut=(int)(timeSecond/60);
        timeSecond-=minut*60;
        int second=(int)timeSecond;

        hourMinutSecond[0]=hour;
        hourMinutSecond[1]=minut;
        hourMinutSecond[2]=second;

        return hourMinutSecond;
    }

}

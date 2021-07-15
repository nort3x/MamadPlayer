package ir.tesla_tic.utils;

public class AmplitudeAverager {
    public static float[] reduced(float[] arr,int sampleRate){
        double w = Double.valueOf(arr.length)/sampleRate;
        float[] ans = new float[sampleRate];

        for (int i = 0; i < sampleRate; i++) {
            ans[i] = average(arr,(int)(i*w),(int)((i+1)*w));
        }
        return ans;
    }

    private static float average(float[] arr,int from,int to){
        float q = 0;
        for (int i = from  ; i < to ; i++) {
            q+=arr[i];
        }
        return q/(to-from);
    }

}

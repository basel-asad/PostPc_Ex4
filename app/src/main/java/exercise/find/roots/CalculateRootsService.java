package exercise.find.roots;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class CalculateRootsService extends IntentService {

  final long ROOT_NOT_FOUND = -1;
  long time_diff;

  public CalculateRootsService() {
    super("CalculateRootsService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent == null) return;
    long timeStartMs = System.currentTimeMillis();
    long numberToCalculateRootsFor = intent.getLongExtra("number_for_service", 0);
    if (numberToCalculateRootsFor <= 0) {
      Log.e("CalculateRootsService", "can't calculate roots for non-positive input" + numberToCalculateRootsFor);
      time_diff = 0;
      broadcast_stopped_calc_intent(numberToCalculateRootsFor);
      return;
    }
    else{
      //calculate the roots.
      long res = get_roots(numberToCalculateRootsFor, timeStartMs);
      if(res == ROOT_NOT_FOUND){
        // how do we get to this, should we overload an "ondestroy" method?
        broadcast_stopped_calc_intent(numberToCalculateRootsFor);
      }
      else{
        // 1) res is numberToCalculateRootsFor ==> numberToCalculateRootsFor is prime
        // 2) res is a smaller number the second factor is numberToCalculateRootsFor/res
        Intent response_intent = new Intent();
        response_intent.setAction("found_roots");
        response_intent.putExtra("original_number",numberToCalculateRootsFor);
        response_intent.putExtra("root1",res);
        response_intent.putExtra("root2",numberToCalculateRootsFor / res);
        response_intent.putExtra("time_till_success",(time_diff)/1000f);
        // "this" is the context
        sendBroadcast(response_intent);
      }
    }


    /*
      examples:
       for input "33", roots are (3, 11)
       for input "30", roots can be (3, 10) or (2, 15) or other options
       for input "17", roots are (17, 1)
       for input "9999991", roots are (9999991, 1)
       for input "829851628752296034247307144300617649465159", after 20 seconds give up

     */
  }

  private void broadcast_stopped_calc_intent(long numberToCalculateRootsFor) {
    Intent response_intent = new Intent();
    response_intent.setAction("stopped_calculations");
    response_intent.putExtra("original_number",numberToCalculateRootsFor);
    response_intent.putExtra("time_until_give_up_seconds",(time_diff)/1000f);
    sendBroadcast(response_intent);
  }

  private long get_roots(Long num, Long start_time){
    /**
     * A simple function to get a root for a number, the root returned can be  2 <= res <= num
     */
    long i = 2;
    long res = ROOT_NOT_FOUND;
    while( i <= num ){
      if(System.currentTimeMillis() - start_time >= 20000){
        time_diff = System.currentTimeMillis() - start_time;
        break;
      }
      if(num % i == 0){
        res = i;
        time_diff = System.currentTimeMillis() - start_time;
        break;
      }
      i++;
    }
    return res;
  }
}
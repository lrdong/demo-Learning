package donglinrong.example;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统计信息
 */
public class Metrics {
    private Map<String, List<Long>> responseTimes = new HashMap<>();
    private Map<String, List<Long>> timestamps = new HashMap<>();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void recordResponseTime(String api, Long responseTime){
        responseTimes.putIfAbsent(api,new ArrayList<>());
        responseTimes.get(api).add(responseTime);
    }

    public void recordTimestamp(String api, Long timestamp){
        timestamps.putIfAbsent(api,new ArrayList<>());
        timestamps.get(api).add(timestamp);
    }

    public void startRepeatedReport(Long period, TimeUnit unit){
        executor.scheduleWithFixedDelay(() -> {
            Gson gson = new Gson();
            Map<String,Map<String,Long>> stats = new HashMap<>();
            for (Map.Entry<String,List<Long>> entry: responseTimes.entrySet() ){
                String api = entry.getKey();
                List<Long> resTime = entry.getValue();
                stats.putIfAbsent(api,new HashMap<>());
                stats.get(api).put("max",max(resTime));
            }

            for (Map.Entry<String,List<Long>> entry : timestamps.entrySet()){
                String api = entry.getKey();
                List<Long> resTime = entry.getValue();
                stats.putIfAbsent(api,new HashMap<>());
                stats.get(api).put("count",Long.parseLong(String.valueOf(resTime.size())));
            }

            System.out.printf(gson.toJson(stats));
        }

        ,0,period,unit);
    }

    private Long max(List<Long> list){
        return list.stream().max((v1,v2) -> (int) (v1 - v2)).get();
    }
}

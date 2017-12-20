package cn.riversky.logAnalyze.app.callback;

import cn.riversky.logAnalyze.app.dao.CacheData;
import cn.riversky.logAnalyze.app.domain.BaseRecord;
import cn.riversky.logAnalyze.storm.dao.LogAnalyzeDao;
import cn.riversky.logAnalyze.storm.domain.LogAnalyzeJob;
import cn.riversky.logAnalyze.storm.utils.DateUtils;
import cn.riversky.logAnalyze.storm.utils.JedisPoolUtils;
import redis.clients.jedis.ShardedJedis;

import java.util.*;

/**
 * @author riversky E-mail:riversky@126.com
 * @version 创建时间 ： 2017/12/19.
 */
public class OneMinuteCallBack implements Runnable {
    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        //24:00分时，将缓存清空
        if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR) == 24) {
            CacheData.setPvMap(new HashMap<String, Integer>());
            CacheData.setUvMap(new HashMap<String, Long>());
        }
        String date = DateUtils.getDate();
        //从redis中获取所有的指标最新的值
        List<BaseRecord> baseRecordList = getBaseRecords(date);
        //根据cacheData获取所有的指标的增量值  用最新全量值减去上一个时间段的全量值
        List<BaseRecord> appendDataList = getAppData(baseRecordList);
        //将增量数据保存到mysql中
        new LogAnalyzeDao().saveMinuteAppendRecord(appendDataList);
    }

    private List<BaseRecord> getAppData(List<BaseRecord> baseRecordList) {
        List<BaseRecord> appDataList=new ArrayList<>();
        for(BaseRecord baseRecord:baseRecordList){
            int pvAppendVal=CacheData.getPv(baseRecord.getPv(),baseRecord.getIndexName());
            long uvAppendValue = CacheData.getUv(baseRecord.getUv(), baseRecord.getIndexName());
            appDataList.add(new BaseRecord(baseRecord.getIndexName(), pvAppendVal, uvAppendValue, baseRecord.getProcessTime()));
        }
        return appDataList;
    }

    private List<BaseRecord> getBaseRecords(String date) {
        List<LogAnalyzeJob> logAnalyzeJobList = new LogAnalyzeDao().loadJobList();
        ShardedJedis jedis = JedisPoolUtils.getShardedJedisPool().getResource();
        List<BaseRecord> baseRecords = new ArrayList<>();
        for (LogAnalyzeJob analyzeJob : logAnalyzeJobList) {
            String pvKey = "log:" + analyzeJob.getJobName() + ":pv:" + date;
            String uvKey = "log:" + analyzeJob.getJobName() + ":uv:" + date;
            String pv = jedis.get(pvKey);
            long uv = jedis.scard(uvKey);
            BaseRecord baseRecord = new BaseRecord(analyzeJob.getJobName(), Integer.parseInt(pv.trim()), uv, new Date());
            baseRecords.add(baseRecord);
        }
        return baseRecords;
    }
}

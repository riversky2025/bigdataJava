package cn.riversky.mahout;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.similarity.precompute.example.GroupLensDataModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 第一代协同过滤算法
 * 迄今为止在个性化推荐系统中，协同过滤（Collaborative Filtering）技术是应用最成功的技术。
 * @author riversky E-mail:riversky@126.com
 * @version 创建时间 ： 2017/12/20.
 */
public class BaseUserRecommender {
    public static void main(String[] args) throws IOException, TasteException {
        //准备数据 这里是电影评分数据
        File file=new File("F:\\大数据\\传智播客\\学习资料架\\day24\\ml-10M100K\\ratings.dat");
        //将数据加载到内存中，GroupLensDataModel是针对开放电影评论数据的
        DataModel dataModel=new GroupLensDataModel(file);
        //计算相似度，相似度有很多种，欧几里德，皮尔逊等等
//        UserSimilarity similarity=new EuclideanDistanceSimilarity(dataModel);
//        UserSimilarity similarity=new PearsonCorrelationSimilarity(dataModel);
        UserSimilarity similarity=new TanimotoCoefficientSimilarity(dataModel);
        //计算最近邻域，邻居有两宗和那个算法，基于固定数量的邻居和基于相似度的邻居，这里使用基于固定数量的邻居
        UserNeighborhood userNeighborhood=new NearestNUserNeighborhood(100,similarity,dataModel);
        //构建推荐器，协同过滤推荐有两种，分别是基于用户的和基于物品的，这里使用基于用户的协同过滤推荐
        Recommender recommender=new GenericUserBasedRecommender(dataModel,userNeighborhood,similarity);
        //给ID为5的用户推荐10部电影
        List<RecommendedItem> recommendedItems=recommender.recommend(5,10);
        System.out.println("使用基于用户的协同过滤算法");
        System.out.println("为用户5推荐10个商品");
        for (RecommendedItem recommendedItem:recommendedItems){
            System.out.println(recommendedItem);
        }
    }
}

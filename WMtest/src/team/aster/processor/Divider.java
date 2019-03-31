package team.aster.processor;

import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;

import java.util.ArrayList;

/****
 * 
 * @author kun
 * 对数据集进行划分
 */
class Divider {
	private static int M = 65535;//字符串Hashcode分布范围
	
	/****
	 * 返回数字绝对值
	 * @param num
	 * @return
	 */
	private static int abs(int num) {
		return num>=0?num:-num;
	}
	
	/****
	 * BKDRHash算法，对字符数组进行散列，结果取绝对值
	 * @param s 字符数组
	 * @return 字符数组对应的Hashcode，非负
	 */
	private static int BKDRHash(char[] s)
	{ 
	    int seed=131 ;// 31 131 1313 13131 131313 etc..  
	    int hash=0;
	    int len = s.length;
	    for(int i=0;i<len;i++) {
	    	hash=hash*seed+s[i];
	    }    
	    return abs(hash % M); 
	}
	
	/****
	 * 根据公式H(Ks||H(p.r||Ks))，计算获取Mac值
	 * @param primaryKey 主键
	 * @param keyCode 密钥
	 * @return Mac值
	 */
	private static int getMac(String primaryKey, String keyCode) {
		char[] stageOne = (primaryKey+keyCode).toCharArray();
		String tmp = keyCode+String.valueOf(BKDRHash(stageOne));
		int result = BKDRHash(tmp.toCharArray());
		return result;
	}


	//todo 用stream API做并行优化
	/****
	 * 数据集划分
	 * @param m 分组数
	 * @param data 数据集，Map类型，key为数据主键，value为主键对应列数据（类型为ArrayList）
	 * @param secretCode 密钥
	 * @return 划分后的数据集map，Map类型，key为划分集合下标，value为ArrayList，包含该集合下所有列数据（类型为ArrayList）
	 */
	static PartitionedDataset divide(int m, DatasetWithPK data, String secretCode){
		PartitionedDataset partitionedDataset = new PartitionedDataset();
		for(String key:data.getDataset().keySet()) {
			int mac = getMac(key,secretCode);
			int index = mac%m;
			ArrayList<String> value = data.getDataset().get(key);
			ArrayList<ArrayList<String>> tmp;
			if(partitionedDataset.containsIndex(index)) {
				tmp = partitionedDataset.getPartitionByIndex(index);
				tmp.add(value);
			}else {
				tmp = new ArrayList<>();
				tmp.add(value);
				partitionedDataset.addToPartition(index, tmp);
			}
		}
		return partitionedDataset;
	}
	
//	public static void main(String[] args) {
//		Map<String,Object>map = new HashMap<String,Object>();
//		ArrayList<String> a1 = new ArrayList<String>();
//		ArrayList<String> a2 = new ArrayList<String>();
//		ArrayList<String> a3 = new ArrayList<String>();
//		ArrayList<String> a4 = new ArrayList<String>();
//		for(int i=0;i<1;i++) {
//			a1.add(new Timer().toString()+"");
//			a2.add(new Timer().toString()+"");
//			a3.add(new Timer().toString()+"");
//			a4.add(new Timer().toString()+"");
//		}
//		map.put("a1", a1);
//		map.put("a2", a2);
//		map.put("a3", a3);
//		map.put("a4", a4);
//		Map<Integer,Object> tmp = Divider.divide(4, map, "vb7d3yf21e");
//		//System.out.println(map);
//		System.out.println(tmp);
//	}
}

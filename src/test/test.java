package test;


import org.apache.commons.dbutils.QueryRunner;

import utils.C3P0Utils;

public class test {
	public static void main(String[] args) throws Exception {
		 QueryRunner qr = new QueryRunner(C3P0Utils.getDataSource());
		 String sql = "INSERT INTO `JavBus` (`NUM`) VALUES ('1sd65')";
		 qr.update(sql);
	}
}

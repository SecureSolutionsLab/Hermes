package zermia.coordinator.clients;

import zermia.common.schedule.MonitorFaultSchedule;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Client {
	private final int clientID;
	private MonitorFaultSchedule schedule;

	public Client (int clientID) {
		this.clientID = clientID;
	}

	public int getID() {
		return clientID;
	}

	public void setFaultSchedule(MonitorFaultSchedule schedule) {
		this.schedule = schedule;
	}

	public MonitorFaultSchedule getFaultSchedule() {
		return this.schedule;
	}

	Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
	String propPath = path + "/replicaStats/";
	boolean fileNewTest = true;

	public void checkForExcelFile() throws IOException {
		String propPath2 = propPath + "Client" + clientID + ".xlsx";
		File baseDir = new File(propPath);
		if(!baseDir.exists()) {
			baseDir.mkdirs();
		}
		File f = new File(propPath2);
		if(f.exists() && !f.isDirectory()) {

		} else {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet("Client");

			//header
			XSSFRow headRow = sheet.createRow(0);
			headRow.createCell(0).setCellValue("Seconds");
			headRow.createCell(1).setCellValue("Throughput");

			try {
				FileOutputStream out = new FileOutputStream(new File(propPath2));
				wb.write(out);
				out.close();
				System.out.println("excel creation sucess");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	public void fillExcelFile2(Integer msgSec, Integer TPC) throws IOException{
		String propPath2 = propPath + "Client" + clientID + ".xlsx";
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(propPath2));
		XSSFSheet sh = wb.getSheet("Client");
		XSSFRow row;
		Integer rowN = sh.getPhysicalNumberOfRows();

		if(fileNewTest) {
			rowN = rowN +1;
			row = sh.createRow(rowN);
			row.createCell(0).setCellValue("NewTest");
			fileNewTest = false;
		}
		rowN = rowN +1;
		row = sh.createRow(rowN);
		row.createCell(0).setCellValue(msgSec);
		row.createCell(1).setCellValue(TPC);

		wb.write(new FileOutputStream(propPath2));
		wb.close();
	}

}

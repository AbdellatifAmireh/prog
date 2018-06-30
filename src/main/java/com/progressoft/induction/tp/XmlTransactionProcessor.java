package com.progressoft.induction.tp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Created by Abdellatif Amireh on 6/6/18.
 */
public class XmlTransactionProcessor implements TransactionProcessor {

	public Transaction[] tra;
	public String[] line;
	public Violation[] viol;
	
	@Override
	public void importTransactions(InputStream in) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			for (;;) {
			    int nread = in.read(buf, 0, buf.length);
			    if (nread <= 0) {
			        break;
			    }
			    baos.write(buf, 0, nread);
			}
			in.close();
			baos.close();
			byte[] bytes = baos.toByteArray();
			String s = "";
			for(byte b: bytes){
				s +=(char)b;
		       }

	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(new InputSource(new StringReader(s)));
	         doc.getDocumentElement().normalize();
	         NodeList nList = doc.getElementsByTagName("Transaction");
	         
	         line = new String[nList.getLength()*3];
	       
	         int temp2 = 0;
	         for (int temp = 0; temp < nList.getLength(); temp++) {
	            Node nNode = nList.item(temp);
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;

	               line[temp2] = eElement.getAttribute("type");
	               line[temp2+1] = eElement.getAttribute("amount");
	               line[temp2+2] = eElement.getAttribute("narration");
	               temp2 +=3;
	            }
	         }
		
			tra = new Transaction[line.length/3];
			viol = new Violation[line.length/3];
			
			int x = 0;
			for(int i =0; i<line.length; i+=3)
			{
				boolean exception = false;
				BigDecimal num = null;
				try
				{
					num = new BigDecimal(line[i+1]);
				}catch(Exception e){
				    exception = true;
				}
					
				int compare = 1;
				if(!exception)
				{
					BigDecimal zero = new BigDecimal(0);
					compare = num.compareTo(zero);
				}
				

				if( (line[i].equals("C") || line[i].equals("D")) &&  (num instanceof BigDecimal) && (line[i+2] instanceof String))           
				{
					tra[x]= new Transaction(line[i], num, line[i+2]);
				}
				
				int order = x+1;
				
				int y =x;
				if(!(num instanceof BigDecimal) || compare != 1)
				{
					viol[x]= new Violation(order, "amount");
					y++;
				}
				if(!(line[i].equals("C")) && !(line[i].equals("D")))
				{
					viol[y]= new Violation(order, "type");
				}
				
				x++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Transaction> getImportedTransactions() {
		List<Transaction> tran = new ArrayList<Transaction>();
			for(int i=0;i<tra.length;i++)
			{
				tran.add(tra[i]);
			}
			return tran;
	}

	@Override
	public List<Violation> validate() {
		List<Violation> vio = new ArrayList<Violation>();
			for(int i=0; i<viol.length;i++)
			{
	
				if(viol[i] != null)
				{
					vio.add(viol[i]);
				}
			}
			return vio;
	}

	@Override
	public boolean isBalanced() {
		double num1 = 0,num2 = 0;
		for(int i=1; i<line.length; i+=3)
		{
			if(line[i-1].equals("C"))
			{
				double x = (double) Double.parseDouble(line[i]);
				num1 += x;
			}
			if(line[i-1].equals("D"))
			{
				double x2 = (double) Double.parseDouble(line[i]);
				num2 += x2;
			}
		}

		if(num1 == num2)
		{
			return true;
		}else
		{
			return false;
		}
	}
}

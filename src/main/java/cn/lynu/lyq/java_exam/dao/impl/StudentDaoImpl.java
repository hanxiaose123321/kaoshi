package cn.lynu.lyq.java_exam.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.lynu.lyq.java_exam.dao.GradeDao;
import cn.lynu.lyq.java_exam.dao.StudentDao;
import cn.lynu.lyq.java_exam.entity.Grade;
import cn.lynu.lyq.java_exam.entity.Student;

@Component("studentDao")
@Transactional

public class StudentDaoImpl implements StudentDao {
	private final static Logger logger = LoggerFactory.getLogger(StudentDaoImpl.class);
	@Resource
	private SessionFactory sessionFactory;
	@Resource
	private GradeDao gradeDao;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public Student findByRegNoAndPassword(String regNo, String password) {
		Query q=sessionFactory.getCurrentSession().createQuery("from Student where registerNo=?0 and password=?1");
		q.setString("0", regNo);
		q.setString("1", password);
		return (Student)q.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<Student> findStudentForSearch(String regNo, String name, Boolean gender, String gradeName) {
		StringBuffer qStrSb =  new StringBuffer("from Student where name like :n ");
		boolean regNoNotNull=false;
		if(regNo!=null && !regNo.equals("")){
			qStrSb.append(" and registerNo like :r ");
			regNoNotNull=true;
		}
		boolean genderNotNull=false;
		if(gender!=null){
			qStrSb.append(" and gender=:g ");
			genderNotNull=true;
		}
		boolean gradeNameNotNull=false;
		if(gradeName!=null && !gradeName.equals("")){
			qStrSb.append(" and grade.name like :g2 ");
			gradeNameNotNull=true;
		}
		Query query = sessionFactory.getCurrentSession().createQuery(qStrSb.toString());
		query.setString("n", "%"+name+"%");
		
		if(regNoNotNull){
			query.setString("r", regNo+"%");
		}
		if(genderNotNull){
			query.setBoolean("g", gender.booleanValue());
		}
		if(gradeNameNotNull){
			query.setString("g2", "%"+gradeName+"%");
		}
		return query.list();
	}
	
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public Student findById(int id) {
		Student s = sessionFactory.getCurrentSession().get(Student.class, id);
		return s;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<Student> findAll() {
		Query q=sessionFactory.getCurrentSession().createQuery("from Student");
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<Student> findByGrade(Grade g){
		Query q=sessionFactory.getCurrentSession().createQuery("from Student where grade=?0");
		q.setParameter("0", g);
		return q.list();
	}
	
	@Override
	public void save(Student s){
		sessionFactory.getCurrentSession().save(s);
	}
	
	@Override
	public void update(Student s){
		sessionFactory.getCurrentSession().update(s);
	}
	
	@Override
	public void delete(Student s){
		sessionFactory.getCurrentSession().delete(s);
	}

	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public int importFromTxt(File file) {
		BufferedReader br= null;
		int cnt = 0;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String fieldNameLine= null;
			String[] fieldNames = null;
			do{
				fieldNameLine=br.readLine();
				if( fieldNameLine==null || fieldNameLine.length()<=1  )  continue;//????????????
				char firstChar=0;
				if((int)(fieldNameLine.trim().charAt(0))==65279){//??????BOM????????????????????????
					firstChar = fieldNameLine.trim().charAt(1);
				}else{
					firstChar = fieldNameLine.trim().charAt(0);
				}
				if( firstChar=='#')  continue; //???????????????
				fieldNameLine=fieldNameLine.replace("??", "");//???UTF-8?????????????????????????????????c2a0
				fieldNames = fieldNameLine.trim().split("[\\s\\p{Punct}]+");//???????????????????????????????????????????????????????????????
				logger.debug("fieldNameLine=["+fieldNameLine+"]");
				if(fieldNames.length>1) break;
			}while(true);
			
			logger.debug("fieldNames="+Arrays.toString(fieldNames));
			
			int regNoIdx=-1, nameIdx=-1, genderIdx=-1, gradeIdx=-1, passwordIdx=-1;
			for(int i=0; i<fieldNames.length; i++){
				switch(fieldNames[i].trim()){
					case "??????":
						regNoIdx=i;
						break;
					case "??????":
						nameIdx=i;
						break;
					case "??????":
						genderIdx=i;
						break;
					case "??????":
						gradeIdx=i;
						break;
					case "??????":
						passwordIdx=i;
						break;
					default:
						logger.debug("??????????????????,?????????"
										   +Arrays.toString(fieldNames)+"???"
										   +fieldNames.length+"?????????????????????");
						break;
				}
			}
			String line = null;
			String regNo=null, gender=null, name=null, grade=null, password=null;
			while( (line=br.readLine())!=null ){
				line=line.replace("??", "");//???UTF-8?????????????????????????????????c2a0
				if(line.trim().length()==0) continue;////????????????
				if(line.charAt(0)=='#') continue; //???????????????
				String[] fields = line.split("[\\s\\p{Punct}]+");
	
				if(fields.length >= fieldNames.length){
					if(regNoIdx!=-1) {
						regNo = fields[regNoIdx].trim();
						regNo=regNo.replace("??", "");
					}
					if(genderIdx!=-1) {
						gender = fields[genderIdx].trim();
						gender=gender.replace("??", "");
					}
					if(nameIdx!=-1) {
						name = fields[nameIdx].trim();
						name=name.replace("??", "");
					}
					if(gradeIdx!=-1) {
						grade = fields[gradeIdx].trim();
						grade=grade.replace("??", "");
					}
					if(passwordIdx!=-1) {
						password= fields[passwordIdx].trim();
						password=password.replace("??", "");
					}
					logger.debug("regNo="+regNo
							+ ",name="+name 
							+ ",gender="+gender 
							+ ",grade="+grade
							+ ",password="+password);
					boolean genderBool = gender.equals("???")?false:true;//?????????"???"???????????????"???"
					
					
					//TODO: change for web?
					//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
					List<Grade> gradeList=gradeDao.findByName(grade);
					Grade theGrade=null;
					if(gradeList!=null && gradeList.size()>0){
						theGrade = gradeList.get(0);//????????????????????????????????????????????????
					}else{
						theGrade = new Grade(grade);
						gradeDao.save(theGrade);
					}
					Student student = new Student(name,regNo,genderBool,password,theGrade);
					sessionFactory.getCurrentSession().save(student);
					cnt++;
				}else{
					logger.debug("?????????"+line+"??? ??????????????????????????????????????????????????????"
										+Arrays.toString(fieldNames));
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("????????????"+cnt+"??????????????????");
		return cnt;
	}

}

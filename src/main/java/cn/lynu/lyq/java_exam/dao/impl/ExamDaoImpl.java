package cn.lynu.lyq.java_exam.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.lynu.lyq.java_exam.dao.BankQuestionDao;
import cn.lynu.lyq.java_exam.dao.ExamDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionDao;
import cn.lynu.lyq.java_exam.entity.BankBlankFillingQuestion;
import cn.lynu.lyq.java_exam.entity.BankChoiceQuestion;
import cn.lynu.lyq.java_exam.entity.BankJudgeQuestion;
import cn.lynu.lyq.java_exam.entity.Exam;
import cn.lynu.lyq.java_exam.entity.ExamQuestion;

@Component("examDao")
@Transactional
public class ExamDaoImpl implements ExamDao {
	private final static Logger logger = LoggerFactory.getLogger(ExamDaoImpl.class);
	@Resource
	private SessionFactory sessionFactory;
	@Resource
	private BankQuestionDao bankQuestionDao;
	@Resource
	private ExamQuestionDao examQuestionDao;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<Exam> findAllFixedExam(){
		Query q=sessionFactory.getCurrentSession().createQuery("from Exam where type=0");
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<String> findAllDistinctExamName() {
		Query q=sessionFactory.getCurrentSession().createQuery("from Exam order by createDate desc");
		List<Exam> examList = q.list();
		Set<String> examNameSet = new HashSet<String>();
		for(Exam ex:examList){
			String exName = ex.getName();
			if(exName.contains("->")){
				examNameSet.add(exName.substring(0, exName.indexOf("->")));
			}else
				examNameSet.add(exName);
		}
		List<String> examNameList = new ArrayList<>(examNameSet);
		Collections.sort(examNameList);
		return examNameList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List<Exam> findByStudentNameAndExamNameAlike(String studentName, String examName){
		Query q=sessionFactory.getCurrentSession().createQuery("from Exam where name like ?0 and name like ?1");
		q.setString("0", "%"+studentName);
		q.setString("1", examName+"%");
		List<Exam> examList = q.list();
		return examList;
	}
	
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public Exam findById(int id) {
		Exam e = sessionFactory.getCurrentSession().get(Exam.class, id);
		return e;
	}
	
	@Override
	public void save(Exam e){
		sessionFactory.getCurrentSession().save(e);
	}
	
	@Override
	public void update(Exam e){
		sessionFactory.getCurrentSession().update(e);
	}
	
	@Override
	public void delete(Exam e){
		sessionFactory.getCurrentSession().delete(e);
	}
	
	@Override
	public void composeExamRandom(Exam exam, int choiceNum,int blankFillingNum, int judgeNum){
		List<BankChoiceQuestion> listChoice = bankQuestionDao.findChoiceWithComposeFlag(1);//??????????????????????????????=1??????
		List<BankBlankFillingQuestion> listBlankFilling = bankQuestionDao.findBlankFillingWithComposeFlag(1);//??????????????????????????????=1??????
		List<BankJudgeQuestion> listJudge = bankQuestionDao.findJudgeWithComposeFlag(1);//??????????????????????????????=1??????
		
		List<BankChoiceQuestion> listChoiceExtracted = extractRandomQuestions(listChoice,choiceNum);
		List<BankBlankFillingQuestion> listBlankFillingExtracted = extractRandomQuestions(listBlankFilling,blankFillingNum);
		List<BankJudgeQuestion> listJudgeExtracted = extractRandomQuestions(listJudge,judgeNum);
		
		logger.debug("listChoiceExtracted="+listChoiceExtracted);
		logger.debug("listBlankFillingExtracted="+listBlankFillingExtracted);
		logger.debug("listJudgeExtracted="+listJudgeExtracted);
		
		for(BankChoiceQuestion q:listChoiceExtracted){
			examQuestionDao.save(new ExamQuestion(exam,q));
		}
		for(BankBlankFillingQuestion q:listBlankFillingExtracted){
			examQuestionDao.save(new ExamQuestion(exam,q));
		}
		for(BankJudgeQuestion q:listJudgeExtracted){
			examQuestionDao.save(new ExamQuestion(exam,q));
		}
	}
	
	/*
	 * ????????????????????????????????????????????????list???????????????num??????????????????
	 */
	private static <T> List<T> extractRandomQuestions(List<T> list,int num){
		int szOriginal = list.size();
		List<T> listExtracted = new ArrayList<>();
		if(szOriginal >= num){
			Random random = new Random();
			for(int i=0; i<num; i++){
				T q=list.get(random.nextInt(szOriginal-i));
				listExtracted.add(q);
				list.remove(q);
			}
		}else{
			logger.debug("?????????????????????:"+num+"?????????????????????????????????["
								+list.get(0).getClass().getSimpleName()+"]????????????:"
								+szOriginal+"????????????????????????");
		}
		return listExtracted;
	}

	@Override
	public void examCreateWithQuestions(Exam exam, List<BankChoiceQuestion> choiceList,
			List<BankBlankFillingQuestion> blankList, List<BankJudgeQuestion> judgeList) {
		this.save(exam);
		
		if(choiceList!=null){
			for(BankChoiceQuestion bq:choiceList){
				ExamQuestion eq=new ExamQuestion(exam,bq);
				examQuestionDao.save(eq);
			}
		}
		if(blankList!=null){
			for(BankBlankFillingQuestion bq:blankList){
				ExamQuestion eq=new ExamQuestion(exam,bq);
				examQuestionDao.save(eq);
			}
		}
		if(judgeList!=null){
			for(BankJudgeQuestion bq:judgeList){
				ExamQuestion eq=new ExamQuestion(exam,bq);
				examQuestionDao.save(eq);
			}
		}
		
	}

}

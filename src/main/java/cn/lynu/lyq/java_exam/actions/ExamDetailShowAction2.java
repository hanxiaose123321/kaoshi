package cn.lynu.lyq.java_exam.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import cn.lynu.lyq.java_exam.common.Constants;
import cn.lynu.lyq.java_exam.common.ExamPhase;
import cn.lynu.lyq.java_exam.common.QuestionType;
import cn.lynu.lyq.java_exam.dao.BankQuestionDao;
import cn.lynu.lyq.java_exam.dao.ExamDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionDao;
import cn.lynu.lyq.java_exam.dao.StudentExamScoreDao;
import cn.lynu.lyq.java_exam.entity.BankBlankFillingQuestion;
import cn.lynu.lyq.java_exam.entity.BankChoiceQuestion;
import cn.lynu.lyq.java_exam.entity.BankJudgeQuestion;
import cn.lynu.lyq.java_exam.entity.Exam;
import cn.lynu.lyq.java_exam.entity.ExamQuestion;
import cn.lynu.lyq.java_exam.entity.Student;
import cn.lynu.lyq.java_exam.entity.StudentExamScore;
import cn.lynu.lyq.java_exam.utils.PropertyUtils;
import cn.lynu.lyq.java_exam.utils.QuestionUtils;

@Component("examDetailShow2")
@Scope("prototype")
public class ExamDetailShowAction2 extends ActionSupport {
	private static final long serialVersionUID = 7703825817401064333L;
	private final static Logger logger = LoggerFactory.getLogger(ExamDetailShowAction2.class);
	@Resource
	private ExamDao examDao;
	@Resource
	private ExamQuestionDao examQuestionDao;
	@Resource 
	private BankQuestionDao bankQuestionDao;
	@Resource
	private StudentExamScoreDao studentExamScoreDao;
	
	private List<BankChoiceQuestion> choiceList=new ArrayList<>();
    private List<BankBlankFillingQuestion> blankFillingList = new ArrayList<>();
    private List<BankJudgeQuestion> judgeList = new ArrayList<>();
    
	private Map<QuestionType,List<Object>> examAnswerMap = new HashMap<>(); //????????????
	
	private int remainingTime; //????????????
	private boolean autoSubmitFlag; //??????????????????
    
	public List<BankChoiceQuestion> getChoiceList() {
		return choiceList;
	}

	public void setChoiceList(List<BankChoiceQuestion> choiceList) {
		this.choiceList = choiceList;
	}

	public List<BankBlankFillingQuestion> getBlankFillingList() {
		return blankFillingList;
	}

	public void setBlankFillingList(List<BankBlankFillingQuestion> blankFillingList) {
		this.blankFillingList = blankFillingList;
	}

	public List<BankJudgeQuestion> getJudgeList() {
		return judgeList;
	}

	public void setJudgeList(List<BankJudgeQuestion> judgeList) {
		this.judgeList = judgeList;
	}
	
	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}
	
	public boolean isAutoSubmitFlag() {
		return autoSubmitFlag;
	}

	public void setAutoSubmitFlag(boolean autoSubmitFlag) {
		this.autoSubmitFlag = autoSubmitFlag;
	}

	@Override
	public String execute() throws Exception {
		//??????action???????????????????????????????????????main.jsp
		ActionContext ctx=ActionContext. getContext();
		if(ctx.getSession().containsKey("USER_INFO")==false){
			this.addActionError("????????????????????????????????????????????????");
			return ERROR;
		}
		
		String	examIds=ctx.getParameters().get("exam_id").getValue();
		String examStrategyIds = ctx.getParameters().get("exam_strategy_id").getValue();
		if(examIds==null){//???????????????????????????????????????examDetail2.jsp?????????
			logger.debug("???examDetail2.jsp?????????");
			examIds =(String) ctx.getSession().get("EXAM_ID");
			examStrategyIds = (String) ctx.getSession().get("EXAM_STRATEGY_ID");
		}else{//?????????????????????
			ctx.getSession().remove("EXAM_SUBMITTED_ANSWER");
		}
		ctx.getSession().put("EXAM_ID", examIds);
		ctx.getSession().put("EXAM_STRATEGY_ID", examStrategyIds);
        int examId=Integer.parseInt( examIds.trim());
        logger.debug("**********"+examId);
        
        Exam exam = examDao.findById(examId);
        remainingTime = exam.getScheduledTime();
        
        //????????????????????????
        Student s1=(Student)ctx.getSession().get("USER_INFO");
        List<StudentExamScore> sesList=studentExamScoreDao.findByStudentAndExam(s1, exam);
		if(sesList.size()>1){
			this.addActionError("????????????????????????????????????(exam_id="+exam.getId()+")????????????????????????");
			return ERROR;
		}else if(sesList.get(0).getExamPhase().equals(ExamPhase.FINAL_SCORED.getChineseName())){
			this.addActionError("?????????????????????????????????????????????????????????????????????");
			return ERROR;
		}else{
			StudentExamScore ses = sesList.get(0);
			Date examStartTime = ses.getExamStartTime();
			Date nowTime = new Date();
			if(examStartTime==null) {//?????????????????????????????????????????????
				ses.setExamStartTime(nowTime);
			}else{
				remainingTime -= (int) ((nowTime.getTime()-examStartTime.getTime())/1000);
			}
			studentExamScoreDao.update(ses);
		}
		
		//??????????????????
		autoSubmitFlag=Boolean.parseBoolean(PropertyUtils.getProperty(Constants.EXAM_TIMEOUT_AUTOSUBMIT));
        
        List<ExamQuestion> eqList = examQuestionDao.findByExam(exam);
        
        choiceList=new ArrayList<>();
        blankFillingList = new ArrayList<>();
        judgeList = new ArrayList<>();
        Map<QuestionType,List<Integer>> eqIdMap = new HashMap<>();
        for(ExamQuestion eq:eqList){
        	if(eq.getQuestionType()==QuestionType.CHOICE.ordinal()){
        		BankChoiceQuestion choiceQ=bankQuestionDao.findChoiceById(eq.getBankChoiceQuestion().getId());
        		//
        		choiceQ.setChoiceA(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceA()));
        		choiceQ.setChoiceB(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceB()));
        		choiceQ.setChoiceC(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceC()));
        		choiceQ.setChoiceD(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceD()));
        		choiceQ.setChoiceE(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceE()));
        		choiceQ.setChoiceF(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceF()));
        		choiceQ.setChoiceG(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceG()));
        		choiceQ.setChoiceH(QuestionUtils.deleteOptionLetter(choiceQ.getChoiceH()));
        		//
        		choiceList.add(choiceQ);
        		
    			List<Object> answersList = examAnswerMap.get(QuestionType.CHOICE);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.CHOICE);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				eqIdList = new ArrayList<Integer>();
    			}
    			answersList.add(choiceQ.getAnswer());
    			examAnswerMap.put(QuestionType.CHOICE, answersList);
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.CHOICE, eqIdList);
    			
        	}else if(eq.getQuestionType()==QuestionType.BLANK_FILLING.ordinal()){
        		BankBlankFillingQuestion blankQ=bankQuestionDao.findBlankFillingById(eq.getBankBlankFillingQuestion().getId());
        		blankFillingList.add(blankQ);
        		
    			List<Object> answersList = examAnswerMap.get(QuestionType.BLANK_FILLING);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.BLANK_FILLING);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				eqIdList = new ArrayList<Integer>();
    			}
    			int blankCnt = countBlank(blankQ.getContent());
    			List<String> currentAnswerList = new ArrayList<>();
    			for(int i=0; i<blankCnt; i++){
    				String theAnswer = getBlankAnswerN(blankQ,i);
    				currentAnswerList.add(theAnswer);
    			}
    			answersList.add(currentAnswerList);
    			examAnswerMap.put(QuestionType.BLANK_FILLING, answersList);
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.BLANK_FILLING, eqIdList);
    			
        	}else if(eq.getQuestionType()==QuestionType.JUDGE.ordinal()){
        		BankJudgeQuestion judgeQ = bankQuestionDao.findJudgeById(eq.getBankJudgeQuestion().getId());
        		judgeList.add(judgeQ);
        		
    			List<Object> answersList = examAnswerMap.get(QuestionType.JUDGE);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.JUDGE);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				eqIdList = new ArrayList<Integer>();
    			}
    			answersList.add(judgeQ.getAnswer());
    			examAnswerMap.put(QuestionType.JUDGE, answersList);
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.JUDGE, eqIdList);
        	}
        }
        logger.debug(examAnswerMap.toString());
        ctx.getSession().put("EXAM_ANSWER", examAnswerMap);
        ctx.getSession().put("EXAM_QUESTION_ID_MAP", eqIdMap);
        logger.debug(eqIdMap.toString());
		return SUCCESS;
	}
	
	/**
	 * ??????????????????____?????????????????????????????????????????????id???name?????????q1_blank2????????????
	 * ????????????1???????????????????????????????????????2???????????????????????????????????????
	 * 
	 * @param content ??????????????????????????????____?????????
	 * @param quesitonNo ???????????????
	 * @return ?????????????????????
	 */
	@SuppressWarnings("unchecked")
	public static String replaceBlank(String content, int questionNo){
		try {
			ActionContext ctx=ActionContext.getContext();
			Map<String,Object> sessionMap = ctx.getSession();
			Map<QuestionType,List<Object>> submittedAnswerMap = (Map<QuestionType,List<Object>>)sessionMap.get("EXAM_SUBMITTED_ANSWER");
			submittedAnswerMap = submittedAnswerMap==null?new HashMap<QuestionType,List<Object>>():submittedAnswerMap;
			
			List<Object> blankAnswerList =  (List<Object>)submittedAnswerMap.get(QuestionType.BLANK_FILLING);
			List<String> blankAnswerListForCurrentQ = null;
			if(blankAnswerList !=null ){
				blankAnswerListForCurrentQ = (List<String>)blankAnswerList.get(questionNo-1);
			}
			content = StringEscapeUtils.escapeHtml(content);
			
			Pattern p = Pattern.compile("[_]{2,}");//??????????????????_?????????????????????  ?????? _______
			Matcher m = p.matcher(content);
			StringBuffer sb = new StringBuffer();
			int i=1;
			while (m.find()) {
				String idStr="'q"+questionNo+"_blank"+i+"'";
				String replacement ="<input type='text' id="+idStr
						+ " name="+idStr+ " style='font-size: 18px; width:200px; text-align:center;' placeholder='????????????' onchange='changeBlank("+questionNo+")'" ;
				if(blankAnswerListForCurrentQ!=null && blankAnswerListForCurrentQ.size()>0){
					String currentBlankAnswer = blankAnswerListForCurrentQ.get(i-1);
					currentBlankAnswer = currentBlankAnswer!=null?currentBlankAnswer:"";
					replacement += "value='"+ currentBlankAnswer +"' />";
				}else {
					replacement += " />";
				}
			    m.appendReplacement(sb, replacement);
			    i++;
			}
			m.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			logger.debug("replaceBlank exception:"+e);
			e.printStackTrace();
			return "";
		}
	}
	
	/*
	 * ??????????????????????????????
	 */
	private int countBlank(String content){
		Pattern p = Pattern.compile("[_]{2,}");//??????????????????_??????????????????
		Matcher m = p.matcher(content);
		int cnt=0;
		while (m.find()) {
		    cnt++;
		}
		return cnt;
	}
	
	/*
	 * ??????????????????????????????????????????, idx???0??????
	 */
	private String getBlankAnswerN(BankBlankFillingQuestion q, int idx){
		String answer = null;
		switch(idx){
			case 0:
				answer = q.getAnswer();
				break;
			case 1:
				answer = q.getAnswer2();
				break;
			case 2:
				answer = q.getAnswer3();
				break;
			case 3:
				answer = q.getAnswer4();
				break;
			case 4:
				answer = q.getAnswer5();
				break;
			case 5:
				answer = q.getAnswer6();
				break;
			case 6:
				answer = q.getAnswer7();
				break;
			case 7:
				answer = q.getAnswer8();
				break;
			default:
				answer = q.getAnswer();
				break;
		}
		return answer;
	}
	
	/*
	 * ????????????????????????????????????1????????????,--->????????????
	 */
	public static boolean isMultipleChoice1(String choiceAnswer){
		if(choiceAnswer!=null && choiceAnswer.contains(",")){ //?????????????????????????????????????????????????????????
			return true;
		}else{
			return false;
		}
	}
	
	/*
	 * ????????????????????????????????????2????????????
	 */
	public static boolean isMultipleChoice2(String choiceContent){
		if(choiceContent!=null && choiceContent.contains("????????????")){ //?????????????????????????????? ??????
			return true;
		}else{
			return false;
		}
	}
	
	/*
	 * ??????session??????????????????????????????
	 */
	public static String determineChecked(int index, String option){
		ActionContext ctx=ActionContext.getContext();
		Map<String,Object> sessionMap = ctx.getSession();
		@SuppressWarnings("unchecked")
		Map<QuestionType,List<Object>> submittedAnswerMap = (Map<QuestionType,List<Object>>)sessionMap.get("EXAM_SUBMITTED_ANSWER");
		submittedAnswerMap = submittedAnswerMap==null?new HashMap<QuestionType,List<Object>>():submittedAnswerMap;
		
		List<Object> choiceAnswerList =  (List<Object>)submittedAnswerMap.get(QuestionType.CHOICE);
		choiceAnswerList = choiceAnswerList!=null?choiceAnswerList:new ArrayList<Object>();
		
		if(choiceAnswerList.get(index).toString().contains(option)){
			return "checked";
		}else{
			return "";
		}
	}
	
	/*
	 * ??????session??????????????????????????????
	 */
	public static String determineChoiceAnswer(int index){
		ActionContext ctx=ActionContext.getContext();
		Map<String,Object> sessionMap = ctx.getSession();
		@SuppressWarnings("unchecked")
		Map<QuestionType,List<Object>> submittedAnswerMap = (Map<QuestionType,List<Object>>)sessionMap.get("EXAM_SUBMITTED_ANSWER");
		submittedAnswerMap = submittedAnswerMap==null?new HashMap<QuestionType,List<Object>>():submittedAnswerMap;
		
		List<Object> choiceAnswerList =  (List<Object>)submittedAnswerMap.get(QuestionType.CHOICE);
		
		if(choiceAnswerList!=null && choiceAnswerList.size()>0){
			return choiceAnswerList.get(index).toString();
		}else{
			return "";
		}
	}
	
	/*
	 * ??????session????????????????????????????????????
	 */
	public static String determineJudgeChecked(int index){
		ActionContext ctx=ActionContext.getContext();
		Map<String,Object> sessionMap = ctx.getSession();
		@SuppressWarnings("unchecked")
		Map<QuestionType,List<Object>> submittedAnswerMap = (Map<QuestionType,List<Object>>)sessionMap.get("EXAM_SUBMITTED_ANSWER");
		submittedAnswerMap = submittedAnswerMap==null?new HashMap<QuestionType,List<Object>>():submittedAnswerMap;
		
		List<Object> judgeAnswerList =  (List<Object>)submittedAnswerMap.get(QuestionType.JUDGE);
		if(judgeAnswerList!=null && judgeAnswerList.size()>0){
			return judgeAnswerList.get(index).toString();
		}else{
			return "";
		}
	}
	
	/*
	 * ?????????????????????????????? ???[[[xxxx.xxx]]]???????????????????????????<img src='images/xxxx.xxx'>???Html???????????????????????????
	 */
	public  static String showContentWithImage(String content){
		content = StringEscapeUtils.escapeHtml(content);
		Pattern pattern = Pattern.compile("\\[{3}\\w+\\.\\w+\\]{3}"); //?????? ???[[[xxxx.xxx]]]???
		Matcher matcher = pattern.matcher(content);
		String picFileName = null;
		if(matcher.find()){
			picFileName = matcher.group();
			picFileName = picFileName.substring(3, picFileName.length()-3);
			String beforePicNameContent = content.substring(0,matcher.start());
			String afterPicNameContent = content.substring(matcher.end());
			String newContent = beforePicNameContent 
					+ "<br><br><img src='images/"+ picFileName +"' style='float:left;clear:both;'>"
					+ afterPicNameContent;
			return newContent;
		}else{
			return content;
		}
	}
	
}

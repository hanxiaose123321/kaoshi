package cn.lynu.lyq.java_exam.actions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import cn.lynu.lyq.java_exam.common.QuestionType;
import cn.lynu.lyq.java_exam.dao.BankQuestionDao;
import cn.lynu.lyq.java_exam.dao.ExamDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionAnswerDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionDao;
import cn.lynu.lyq.java_exam.dao.ExamStrategyDao;
import cn.lynu.lyq.java_exam.dao.StudentDao;
import cn.lynu.lyq.java_exam.entity.BankBlankFillingQuestion;
import cn.lynu.lyq.java_exam.entity.BankChoiceQuestion;
import cn.lynu.lyq.java_exam.entity.BankJudgeQuestion;
import cn.lynu.lyq.java_exam.entity.Exam;
import cn.lynu.lyq.java_exam.entity.ExamQuestion;
import cn.lynu.lyq.java_exam.entity.ExamQuestionAnswer;
import cn.lynu.lyq.java_exam.entity.ExamStrategy;
import cn.lynu.lyq.java_exam.entity.Student;
import cn.lynu.lyq.java_exam.utils.QuestionUtils;

@Component("downloadExportedFile")
@Scope("prototype")
public class DownloadExportedFileAction extends ActionSupport {

	private static final long serialVersionUID = 8248962048889948056L;
	private final static Logger logger = LoggerFactory.getLogger(DownloadExportedFileAction.class);
	
	private InputStream inputStream;
	private String fileName;
	
	@Resource
	private ExamDao examDao;
	@Resource
	private StudentDao studentDao;
	@Resource
	private ExamStrategyDao examStrategyDao;
	@Resource
	private ExamQuestionDao examQuestionDao;
	@Resource
	private ExamQuestionAnswerDao examQuestionAnswerDao;
	@Resource 
	private BankQuestionDao bankQuestionDao;
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	private List<BankChoiceQuestion> choiceList=new ArrayList<>();
	private List<BankBlankFillingQuestion> blankFillingList = new ArrayList<>();
	private List<BankJudgeQuestion> judgeList = new ArrayList<>();

	@Override
	public String execute() throws Exception {
		logger.info("???????????????word??????(*.docx)");
		ActionContext ctx=ActionContext. getContext();
		String stuIds = ctx.getParameters().get("stu_id").getValue();
        String examIds=ctx.getParameters().get("exam_id").getValue();
        String examStrategyIds=ctx.getParameters().get("exam_strategy_id").getValue();
        int stuId=Integer.parseInt( stuIds.trim());
        int examId=Integer.parseInt( examIds.trim());
        int examStrategyId=Integer.parseInt( examStrategyIds.trim());
        logger.debug("**********"+stuId);
        logger.debug("**********"+examId);
        logger.debug("**********"+examStrategyId);
		
		
		Student student = studentDao.findById(stuId);
        Exam exam = examDao.findById(examId);
        
        List<ExamQuestion> eqList = examQuestionDao.findByExam(exam);
        ExamStrategy examStrategy = examStrategyDao.findById(examStrategyId);
        
        choiceList=new ArrayList<>();
        blankFillingList = new ArrayList<>();
        judgeList = new ArrayList<>();
        
        Map<QuestionType,List<Integer>> eqIdMap = new HashMap<>();
        Map<QuestionType,List<Object>> examAnswerMap = new HashMap<>();// ????????????
        Map<QuestionType,List<Object>> examQuestionAnswerMap = new HashMap<>(); //???????????????
        
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
    			List<Object> questionAnswersList = examQuestionAnswerMap.get(QuestionType.CHOICE);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.CHOICE);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				questionAnswersList = new ArrayList<Object>();
    				eqIdList = new ArrayList<Integer>();
    			}
    			answersList.add(choiceQ.getAnswer());
    			ExamQuestionAnswer eqa = examQuestionAnswerDao.findByStudentAndExamQuestion(student,eq);
    			questionAnswersList.add(eqa.getAnswer());
    			
    			examQuestionAnswerMap.put(QuestionType.CHOICE, questionAnswersList);
    			examAnswerMap.put(QuestionType.CHOICE, answersList);
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.CHOICE, eqIdList);
        	}else if(eq.getQuestionType()==QuestionType.BLANK_FILLING.ordinal()){
        		BankBlankFillingQuestion blankQ=bankQuestionDao.findBlankFillingById(eq.getBankBlankFillingQuestion().getId());
        		blankFillingList.add(blankQ);
        		
    			List<Object> answersList = examAnswerMap.get(QuestionType.BLANK_FILLING);
    			List<Object> questionAnswersList = examQuestionAnswerMap.get(QuestionType.BLANK_FILLING);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.BLANK_FILLING);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				questionAnswersList = new ArrayList<Object>();
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
    			
    			ExamQuestionAnswer eqa = examQuestionAnswerDao.findByStudentAndExamQuestion(student,eq);
    			questionAnswersList.add(getQuestionAnswerListForBlank(eqa.getAnswer()));
    			examQuestionAnswerMap.put(QuestionType.BLANK_FILLING, questionAnswersList);
    			
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.BLANK_FILLING, eqIdList);
        	}else if(eq.getQuestionType()==QuestionType.JUDGE.ordinal()){
        		BankJudgeQuestion judgeQ = bankQuestionDao.findJudgeById(eq.getBankJudgeQuestion().getId());
        		judgeList.add(judgeQ);
        		
    			List<Object> answersList = examAnswerMap.get(QuestionType.JUDGE);
    			List<Object> questionAnswersList = examQuestionAnswerMap.get(QuestionType.JUDGE);
    			List<Integer> eqIdList = eqIdMap.get(QuestionType.JUDGE);
    			if(answersList==null){
    				answersList = new ArrayList<Object>();
    				questionAnswersList = new ArrayList<Object>();
    				eqIdList = new ArrayList<Integer>();
    			}
    			answersList.add(judgeQ.getAnswer());
    			examAnswerMap.put(QuestionType.JUDGE, answersList);
    			
    			ExamQuestionAnswer eqa = examQuestionAnswerDao.findByStudentAndExamQuestion(student,eq);
    			questionAnswersList.add(eqa.getAnswer());
    			examQuestionAnswerMap.put(QuestionType.JUDGE, questionAnswersList);
    			
    			eqIdList.add(eq.getId());
    			eqIdMap.put(QuestionType.JUDGE, eqIdList);
        	}
        }
        String paperName = exam.getName();
        String tmpFilePath = generateDocxFromPaper( paperName, choiceList, blankFillingList, judgeList, examQuestionAnswerMap, examStrategy );
        
        inputStream = new FileInputStream(tmpFilePath);
		logger.debug("*******inputStream="+inputStream);
		fileName=new String((paperName+".docx").getBytes(),"ISO8859-1");
		return SUCCESS;
	}
	
	private String generateDocxFromPaper(String paperName, List<BankChoiceQuestion> choiceList,	List<BankBlankFillingQuestion> blankFillingList,
	 List<BankJudgeQuestion> judgeList,  Map<QuestionType,List<Object>> examQuestionAnswerMap, ExamStrategy examStrategy){
		
		XWPFDocument xdoc = new XWPFDocument();
		//????????????
		XWPFParagraph xparaTitle = xdoc.createParagraph();
		xparaTitle.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun titleRun = xparaTitle.createRun();
		titleRun.setFontFamily("KaiTi");//?????????????????????
		titleRun.setBold(true);
		titleRun.setFontSize(20);
		titleRun.setText(paperName);
		
		XWPFParagraph xpara = xdoc.createParagraph();
		
		//?????????
		if(choiceList!=null && choiceList.size()>0){
			xpara = xdoc.createParagraph();
			XWPFRun run = xpara.createRun();
			run.setBold(true); // ??????
			run.setText("??????????????????"+choiceList.size()+"?????????????????????"+examStrategy.getChoicePerScore()+"??????");
			
			for(int i=0; i<choiceList.size(); i++){
				//??????
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText((i+1)+".  ");
				//??????
				BankChoiceQuestion bq = choiceList.get(i);
				run = xpara.createRun();
				//run.setText(bq.getContent());
				showContentWithImageInDoc(run, bq.getContent());
				
				//??????A
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText("A. ");
				run = xpara.createRun();
				run.setText(bq.getChoiceA());
				//??????B
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText("B. ");
				run = xpara.createRun();
				run.setText(bq.getChoiceB());
				//??????C
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText("C. ");
				run = xpara.createRun();
				run.setText(bq.getChoiceC());
				//??????D
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText("D. ");
				run = xpara.createRun();
				run.setText(bq.getChoiceD());
				//??????
				String correctAnswer=bq.getAnswer();
				String yourAnswer = (String)(examQuestionAnswerMap.get(QuestionType.CHOICE).get(i));
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				if(yourAnswer==null || yourAnswer!=null && false==yourAnswer.trim().equals(correctAnswer.trim())){
					run.setColor("FF0000");
				}
				run.setText("??????????????????"+yourAnswer+"?????????????????????"+correctAnswer);
				xpara = xdoc.createParagraph();//????????????
			}
			
		}
		
		//?????????
		if(blankFillingList!=null && blankFillingList.size()>0){
			xpara = xdoc.createParagraph();
			XWPFRun run = xpara.createRun();
			run.setBold(true); // ??????
			run.setText("??????????????????"+blankFillingList.size()+"?????????????????????"+examStrategy.getBlankPerScore()+"??????");
			
			for(int i=0; i<blankFillingList.size(); i++){
				//??????
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText((i+1)+".  ");
				//??????
				BankBlankFillingQuestion bq = blankFillingList.get(i);
				run = xpara.createRun();
				run.setText(bq.getContent());
				//??????
				String correctAnswer=bq.getAnswer();
				@SuppressWarnings("unchecked")
				String yourAnswer =((List<String>)(examQuestionAnswerMap.get(QuestionType.BLANK_FILLING).get(i))).get(0);
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				if(yourAnswer==null || yourAnswer!=null && false==yourAnswer.trim().equals(correctAnswer.trim())){
					run.setColor("FF0000");
				}
				run.setText("??????????????????"+yourAnswer+"?????????????????????"+correctAnswer);
				xpara = xdoc.createParagraph();//????????????
			}
		}
		
		//?????????
		if(judgeList!=null && judgeList.size()>0){
			xpara = xdoc.createParagraph();
			XWPFRun run = xpara.createRun();
			run.setBold(true); // ??????
			run.setText("??????????????????"+judgeList.size()+"?????????????????????"+examStrategy.getJudgePerScore()+"??????");
			
			for(int i=0; i<judgeList.size(); i++){
				//??????
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				run.setBold(true); // ??????
				run.setText((i+1)+".");
				//??????
				BankJudgeQuestion bq = judgeList.get(i);
				run = xpara.createRun();
				run.setText(bq.getContent());
				//??????
				String correctAnswer=bq.getAnswer();
				String yourAnswer = (String)(examQuestionAnswerMap.get(QuestionType.JUDGE).get(i));
				xpara = xdoc.createParagraph();
				run = xpara.createRun();
				if(yourAnswer==null || yourAnswer!=null && false==yourAnswer.trim().equals(correctAnswer.trim())){
					run.setColor("FF0000");
				}
				run.setText("??????????????????"+yourAnswer+"?????????????????????"+correctAnswer);
				xpara = xdoc.createParagraph();//????????????
			}
		}
		
		
		try {
			File tmpFile = File.createTempFile(Long.toString(new Date().getTime()),".docx");
			OutputStream os = new FileOutputStream(tmpFile);
			xdoc.write(os);
			xdoc.close();
			return tmpFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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
	
	private List<String> getQuestionAnswerListForBlank(String blankQuestionAnswerStr){
		List<String> answerList = new ArrayList<>();
		String[] answersArray = blankQuestionAnswerStr.split(",");
		for(String answer: answersArray){
			int endIdx = answer.indexOf("]]]");
			answerList.add(answer.trim().substring(3,endIdx));
		}
		return answerList;
	}
	
	/*
	 * ?????????????????????????????? ???[[[xxxx.xxx]]]???????????????????????????<img src='images/xxxx.xxx'>???Html???????????????????????????
	 */
	public  static void showContentWithImageInDoc(XWPFRun run, String content){
//		content = StringEscapeUtils.escapeHtml(content);
		Pattern pattern = Pattern.compile("\\[{3}\\w+\\.\\w+\\]{3}"); //?????? ???[[[xxxx.xxx]]]???
		Matcher matcher = pattern.matcher(content);
		String picFileName = null;
		if(matcher.find()){
			picFileName = matcher.group();
			picFileName = picFileName.substring(3, picFileName.length()-3);
			String beforePicNameContent = content.substring(0,matcher.start());
			String afterPicNameContent = content.substring(matcher.end());
			
			run.setText(beforePicNameContent);
			run.addCarriageReturn();
			String picFilePath = ServletActionContext.getServletContext().getRealPath("images/"+picFileName);
			addPictureToRun(run, new File(picFilePath));
			run.setText(afterPicNameContent);
			run.addCarriageReturn();
		}else{
			run.setText(content);
		}
	}
	
	public static void addPictureToRun(XWPFRun run, File picFile){
		try {
			BufferedImage bi = ImageIO.read(picFile);
			FileInputStream fis = new FileInputStream(picFile);
			
			int picType = XWPFDocument.PICTURE_TYPE_PNG;
			if(picFile.getName().endsWith(".jpg")){
				picType = XWPFDocument.PICTURE_TYPE_JPEG;
			}else if(picFile.getName().endsWith(".png")){
				picType = XWPFDocument.PICTURE_TYPE_PNG;
			}else if(picFile.getName().endsWith("gif")){
				picType = XWPFDocument.PICTURE_TYPE_GIF;
			}
//			System.out.println(bi.getWidth());
//			System.out.println(bi.getHeight());
			run.addPicture(fis, picType, picFile.getAbsolutePath(), Units.toEMU(bi.getWidth()*0.6667), 
					Units.toEMU(bi.getHeight()*0.6667));
			fis.close();
		} catch (InvalidFormatException|IOException e) {
			e.printStackTrace();
		} 
	}
}

package cn.lynu.lyq.java_exam.actions;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import cn.lynu.lyq.java_exam.dao.BankQuestionDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionAnswerDao;
import cn.lynu.lyq.java_exam.dao.ExamQuestionDao;
import cn.lynu.lyq.java_exam.entity.BankBlankFillingQuestion;
import cn.lynu.lyq.java_exam.entity.BankChoiceQuestion;
import cn.lynu.lyq.java_exam.entity.BankJudgeQuestion;
import cn.lynu.lyq.java_exam.entity.ExamQuestion;
import cn.lynu.lyq.java_exam.entity.ExamQuestionAnswer;
import cn.lynu.lyq.java_exam.entity.Grade;
import cn.lynu.lyq.java_exam.entity.Student;

@Component("questionPieChart")
@Scope("prototype")
public class QuestionPieChartAction extends ActionSupport {
	private static final long serialVersionUID = 6053800919739644090L;
	private final static Logger logger = LoggerFactory.getLogger(QuestionPieChartAction.class);

	private JFreeChart chart;
	
	private int type;
	private int bankQuestionId;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBankQuestionId() {
		return bankQuestionId;
	}

	public void setBankQuestionId(int bankQuestionId) {
		this.bankQuestionId = bankQuestionId;
	}

	@Resource
	private BankQuestionDao bankQuestionDao;
	@Resource
	private ExamQuestionDao examQuestionDao;
	@Resource
	private ExamQuestionAnswerDao examQuestionAnswerDao;

	public JFreeChart getChart() {
		return chart;
	}

	public void setChart(JFreeChart chart) {
		this.chart = chart;
	}

	@Override
	public String execute() throws Exception {
		ActionContext ctx = ActionContext.getContext();
		logger.info("?????????");
		Student theStudent =(Student)ctx.getSession().get("USER_INFO");
		
		String typeStr = ctx.getParameters().get("type").getValue();
		if(typeStr==null || typeStr.equals("")) {
			type = 0;
		}else{
			type = Integer.parseInt(typeStr.trim());
		}
		
		String bankQuestionIds = ctx.getParameters().get("bq_id").getValue();
		if(bankQuestionIds==null || bankQuestionIds.equals(""))  {
			bankQuestionId = 1;
		}else{
			bankQuestionId = Integer.parseInt(bankQuestionIds.trim());
		}
		
		Map<String,Integer> answerMap = new HashMap<>();
		Map<String,List<String>> studentAnswerMap = new HashMap<>(); // ???answerMap???key?????????????????????value?????????????????????
		List<ExamQuestion>  eqList= null;
		String correctAnswer = null;
		switch (type) {
			case 0: //?????????
				BankChoiceQuestion bq1 = bankQuestionDao.findChoiceById(bankQuestionId);
				correctAnswer = bq1.getAnswer();
				eqList = examQuestionDao.findByBankChoiceQuestion(bq1);
				break;
			case 1: //?????????
				BankBlankFillingQuestion bq2 = bankQuestionDao.findBlankFillingById(bankQuestionId);
				correctAnswer = bq2.getAnswer();
				eqList = examQuestionDao.findByBankBlankFillingQuestion(bq2);
				break;
			case 2: //?????????
				BankJudgeQuestion bq3= bankQuestionDao.findJudgeById(bankQuestionId);
				correctAnswer = bq3.getAnswer();
				eqList = examQuestionDao.findByBankJudgeQuestion(bq3);
				break;
		}
		for(ExamQuestion eq:eqList){
			List<ExamQuestionAnswer> eqaList= examQuestionAnswerDao.findByExamQuestion(eq);
			for(ExamQuestionAnswer eqa: eqaList){
				Grade grade = eqa.getStudent().getGrade();
				String gradeName = grade!=null?grade.getName():"";
				if(gradeName!=null && gradeName.equals(theStudent.getGrade().getName()) ){
					String answer = eqa.getAnswer();
					if(answer!=null) answer = answer.trim();
					if (answer!=null && type==1) {
						answer = answer.substring(answer.lastIndexOf('[') + 1,answer.indexOf(']'));
					}
//					logger.debug("&&&&&&&&&&&&&&"+answer+"&&&&&&&&&&&&&&&");
					if( answerMap.containsKey(answer)){
						int cnt = answerMap.get(answer);
						answerMap.put(answer, ++cnt);
					}else{
						answerMap.put(answer,1);
					}
					List<String> stuList = studentAnswerMap.get(answer);
					if(stuList==null) stuList = new ArrayList<>();
					stuList.add(eqa.getStudent().getName());
					studentAnswerMap.put(answer, stuList);
				}
			}
		}
		
		
//		if(bq instanceof BankBlankFillingQuestion){
//			correctAnswer = correctAnswer.substring(correctAnswer.lastIndexOf('[')+1, correctAnswer.indexOf(']'));
//		}
		
		DefaultPieDataset dataset = new DefaultPieDataset();
//		dataset.setValue("B?????????,?????????", new Double(20));
//		dataset.setValue("A?????????,?????????", new Double(20));
//		dataset.setValue("C?????????,??????,??????,?????????", new Double(40));
//		dataset.setValue("D????????????", new Double(10));
		int totalCnt = 0, correctCnt = 0; //???????????????????????????
		for(Map.Entry<String, Integer> entry: answerMap.entrySet()){
			String answerKey = entry.getKey();
			Integer cnt = entry.getValue();
			totalCnt += cnt;
			if(answerKey!=null && answerKey.trim().equals(correctAnswer)) correctCnt = cnt;
			String stuListStr = studentAnswerMap.get(answerKey).toString();
			stuListStr = stuListStr.substring(1,stuListStr.lastIndexOf(']'));
			dataset.setValue( (answerKey==null?"?????????":answerKey) + "???" + cnt + "?????????" + stuListStr + "???", cnt.doubleValue());
		}
		
		chart = ChartFactory.createPieChart("?????????????????????"+correctAnswer+",????????????????????????"+(double)correctCnt/totalCnt*100 + "%",
								dataset,false, false, false);
		setChartProperties(chart);
		return SUCCESS;
	}

	/**
	 * ??????Chart???????????????????????????
	 */
	private void setChartProperties(JFreeChart chart) {

		// ?????????????????????????????????????????????:
		TextTitle textTitle = chart.getTitle();
		textTitle.setFont(new Font("SimHei", Font.PLAIN, 20));
		LegendTitle legend = chart.getLegend();
		if (legend != null) {
			legend.setItemFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		}
		PiePlot pie = (PiePlot) chart.getPlot();
		pie.setLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
		pie.setNoDataMessage("No data available");
		// ??????PieChart?????????????????????
		pie.setCircular(true);
		// ??????
		pie.setLabelGap(0.05D);
	}
}

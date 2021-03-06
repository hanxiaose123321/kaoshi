<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Material Components Test Page 7: Forms </title>
    <link type="text/css" rel="stylesheet" href="css/materialize.min.css">
    <link type="text/css" rel="stylesheet" href="css/material_icons.css">
    <link type="text/css" rel="stylesheet" href="css/nouislider.css">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style type="text/css">
        body {
            font-family: Roboto, "Microsoft YaHei";
            font-size: large;
        }
        .mytable{
        	width: 80%;
        	margin: 0 auto;
        }
        
        #userbar{
        	position: absolute;
        	top: 0;
        	right: 5px;
        	width: 280px;
        	height: 35px;
        	text-align: center;
        	border-bottom: 1px solid blue;
        }
        #userpanel{
        	position: absolute;
        	text-align: left;
        	top: 55px;
        	right: 5px;
        	width: 320px;
        	height: 140px;
        	border-radius: 10px;
        	background: #eeeeff;
        	display: none;
        }
        #userbar:hover #userpanel,  #userbar:hover .arr_up{
        	cursor: pointer;
        	display: block;
        }
        .arr_up{
        	text-align: left;
        	position:relative;
        	top: 35px;
        	left: 40px;
        	display: none;
        }
        .arr_up a{
			border-color: transparent  transparent #eeeeff transparent ;
			border-width: 0 10px 20px 10px;
			border-style: solid;
			position: absolute;
		}
		
        #userpanel img{
             display:block;
             position:absolute;
             top:10px;
             left:10px;
             width:120px;
             height:120px;
             border:none;
         }
         #userpanel dl{
             margin-top: 10px;
             width:190px;
             color:#999;
             font-size: medium;
             float:right;
         }
         #userpanel dl dd{
         	margin-left: 10px;
         }
         #userpanel dl dd span {
             font-weight: bold;
             padding-right: 5px;
             color: #639;
         }	
    </style>
</head>
<body>
	<div id="userbar">
		<span class="arr_up">
			<a href="javascript:;"></a>
		</span>
		????????????????????????(20160001)
		<div id="userpanel">
			<img src="images/soccer4.jpg" alt="" />
            <dl>
	            <dd><span>??????:</span>?????????</dd>
	            <dd><span>??????:</span>20160001</dd>
	            <dd><span>??????:</span>???</dd>
	            <dd><span>??????:</span>2016??????????????????</dd>
	            <dd><span>??????:</span>??????Java</dd>
            </dl>
		</div>
	</div>
	
	<div class="container">
	<form class="col s12" name="form1">
	<h4>?????????</h4>
	<table class="mytable">
		<tr>
			<td width="50px"><span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">1.</span></td>
			<td>Java?????????????????????</td>
			<td>
				<span id="choice_answer_q1"class="blue-text text-darken-2"></span>
			</td>
		</tr>
		<tr><td colspan="3">
			<table>
				<tr>
					<td width="60px">
						<input type="radio" name="choice_q1" id="q1_choiceA" class="with-gap" value="A" onclick="changeChoice(1)">
						<label for="q1_choiceA">A.</label>
					</td>
					<td> Java SE </td>
				</tr>
				<tr>
					<td width="60px">
						<input type="radio" name="choice_q1" id="q1_choiceB" class="with-gap" value="B" onclick="changeChoice(1)">
						<label for="q1_choiceB">B.</label>
					</td>
					<td> Java EE </td>
				</tr>
				<tr>
					<td width="60px">
						<input type="radio" name="choice_q1" id="q1_choiceC" class="with-gap" value="C" onclick="changeChoice(1)">
						<label for="q1_choiceC">C.</label>
					</td>
					<td> Java ME </td>
				</tr>
				<tr>
					<td width="60px">
						<input type="radio" name="choice_q1" id="q1_choiceD" class="with-gap" value="D" onclick="changeChoice(1)">
						<label for="q1_choiceD">D.</label>
					</td>
					<td> Java UE </td>
				</tr>
			</table>
		</td></tr>
				<tr>
					<td width="50px"><span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">2.</span></td>
					<td>Java?????????????????????</td>
					<td>
						<span id="choice_answer_q2"class="blue-text text-darken-2"></span>
					</td>
				</tr>
				<tr><td colspan="3">
					<table>
						<tr>
							<td width="60px">
								<input type="radio" name="choice_q2" id="q2_choiceA" class="with-gap" value="A" onclick="changeChoice(2)">
								<label for="q2_choiceA">A.</label>
							</td>
							<td> Java SE </td>
						</tr>
						<tr>
							<td width="60px">
								<input type="radio" name="choice_q2" id="q2_choiceB" class="with-gap" value="B" onclick="changeChoice(2)">
								<label for="q2_choiceB">B.</label>
							</td>
							<td> Java EE </td>
						</tr>
						<tr>
							<td width="60px">
								<input type="radio" name="choice_q2" id="q2_choiceC" class="with-gap" value="C" onclick="changeChoice(2)">
								<label for="q2_choiceC">C.</label>
							</td>
							<td> Java ME </td>
						</tr>
						<tr>
							<td width="60px">
								<input type="radio" name="choice_q2" id="q2_choiceD" class="with-gap" value="D" onclick="changeChoice(2)">
								<label for="q2_choiceD">D.</label>
							</td>
							<td> Java UE </td>
						</tr>
					</table>
			<div class="row">
				<div class="divider" style="height: 10px;background-color: #fff;"></div>
				<div class="col s12">
					<button class="teal darken-4 waves-effect waves-teal btn-flat" type="submit" name="action">
						<span class="yellow-text text-lighten-1">Submit
		        		<i class="material-icons right">send</i></span>
		    		</button>
	    		</div>
    		</div>
		</td></tr>
	</table>
	
	<h4>?????????</h4>
	<table class="mytable">
		<tr>
			<td>
				<span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">1.</span><span>???????????????????????????????????????
				 <input type="text" name="blank1" placeholder="????????????" id="q1_blank1" style="width:200px" class="validate">
				???????????????
				<input type="text" placeholder="????????????" name="blank2" id="q1_blank2" style="width:200px" class="validate">
				???
				</span>
			</td>
		</tr>
		<tr>
			<td>
				<span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">2.</span><span>????????????????????????????????????String??????
				 <input type="text" name="blank1" placeholder="????????????" id="q2_blank1" style="width:150px" class="validate">
				??????????????????????????????????????????Integer??????
				<input type="text" placeholder="????????????" name="blank2" id="q2_blank2" style="width:150px" class="validate">
				?????????
				</span>
			</td>
		</tr>
	</table>
	
	<h4>?????????</h4>
	<table class="mytable">
		<tr>
			<td>
				<span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">1.</span>
				<span>Object????????????????????????</span>
			</td>
			<td>
				<span class="switch">
                    <label>
                       	 ??????
                        <input name="judge_q1" id="judge_q1" type="checkbox"  onchange="changeSwitch(1)">
                        <span class="lever"></span>
                                                                       ??????
                    </label>
                </span>
			</td>
			<td>
				<span class="teal-text text-lighten-1" id="q1_answer">
		        	??????
		        </span>
			</td>
		</tr>
		<tr>
			<td>
				<span class="blue-text text-lighten-2" style="display:inline-block;width:30px;">2.</span>
				<span>Java???????????????????????????????????????	</span>
			</td>
			<td>
				<span class="switch">
                    <label>
                       	 ??????
                        <input name="judge_q2" id="judge_q2" type="checkbox" onchange="changeSwitch(2)">
                        <span class="lever"></span>
                       	 ??????
                    </label>
                </span>
			</td>
			<td>
				<span class="teal-text text-lighten-1" id="q2_answer">
		        	??????
		        </span>
			</td>
		</tr>
	</table>
	</form>
	</div>
	<script type="text/javascript" src="js/nouislider.min.js"></script>
	<script type="text/javascript" src="js/jquery-2.1.1.min.js"></script>
	<script type="text/javascript" src="js/materialize.min.js"></script>
	<script type="text/javascript">
		function changeChoice(idx){
			$("#choice_answer_q"+idx).text(document.form1["choice_q"+idx].value);
		}
		function changeSwitch(idx){
			if($("#judge_q"+idx).prop("checked")==true){
				$("#q"+idx+"_answer").html("<i class='material-icons right small'>done</i>");
			}else{
				$("#q"+idx+"_answer").html("<i class='right'><img src='images/wrong.png' width='20'/></i>");
			}
		}
	</script>
</body>
</html>

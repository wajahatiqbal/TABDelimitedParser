package student_report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Report {

	String ResultedReport = "Student \"<student-name>\" was taught the course \"<course-name>\" by instructors \"<instructor-name>\". He \"<result>\" the exam by getting a score of \"<obtained-marks>\" out of \"<total-marks>\"";

	// Setting up the Hashmaps
	HashMap<Integer, HashMap<String, String>> currentMap = new HashMap<>(), allStudents = new HashMap<>(),
			allExamResults = new HashMap<>(), allTeachers = new HashMap<>(), allCourses = new HashMap<>();
	HashMap<String, String> entity = new HashMap<>();

	public HashMap<Integer, HashMap<String, String>> getCurrentMap() {
		return currentMap;
	}

	public void setCurrentMap(HashMap<Integer, HashMap<String, String>> currentMap) {
		this.currentMap = currentMap;
	}

	public void putCurrentEntityInCurrentMap(boolean initiateAgain) {
		getCurrentMap().put(getCurrentMap().size(), entity);
		if (initiateAgain)
			entity = new HashMap<>();
	}

	int pervSpaceCount = 0;

	// parsing the values
	private void doParse(String line, int spaceCount) {
		if (line.equals("") || line.charAt(0) == '%') {
			return;
		}

		String[] parts = line.split(":");

		if (parts.length == 1) {
			if (parts[0].trim().equals("Teachers")) {
				putCurrentEntityInCurrentMap(true);
				setCurrentMap(allTeachers);
			} else if (parts[0].trim().equals("Courses")) {
				putCurrentEntityInCurrentMap(true);
				setCurrentMap(allCourses);
			} else if (parts[0].trim().equals("Students")) {
				putCurrentEntityInCurrentMap(true);
				setCurrentMap(allStudents);
			} else if (parts[0].trim().equals("ExamResults")) {
				putCurrentEntityInCurrentMap(false);
				setCurrentMap(allExamResults);
			} else {
				putCurrentEntityInCurrentMap(true);
				if (pervSpaceCount > spaceCount && getCurrentMap().equals(allExamResults))
					setCurrentMap(allStudents);
				pervSpaceCount = spaceCount;
			}

		} else {
			entity.put(parts[0].trim(), parts[1].trim());
		}

	}
	
	// setting up the reports for all the records.
	public void doEvaluateAndWrite(BufferedWriter out) throws Exception{
		for (int i = 1; i < allStudents.size(); i++) {
			HashMap<String, String> student = allStudents.get(i);
			HashMap<Integer, HashMap<String, String>> studentResults = getResults(student.get("StudentID"));

			for (int j = 0; j < studentResults.size(); j++) {

				int totalMarks = Integer.parseInt(getCourseTotal(studentResults.get(j).get("Course")));
				int obtainedMarks = Integer.parseInt(studentResults.get(j).get("Marks"));

				String currentReport = ResultedReport.replaceAll("<student-name>", student.get("Name"));
				currentReport = currentReport.replaceAll("<course-name>",
						getCourseName(studentResults.get(j).get("Course")));
				
				currentReport = currentReport.replace("<instructor-name>",
						getInstructorNames(studentResults.get(j).get("Instructors")));
				currentReport = currentReport.replaceAll("<total-marks>", "" + totalMarks);
				currentReport = currentReport.replaceAll("<obtained-marks>", "" + obtainedMarks);
				currentReport = currentReport.replaceAll("<result>",
						((float) obtainedMarks / (float) totalMarks > 0.6) ? "passed" : "failed");

				out.write(currentReport);
				out.newLine();
				out.newLine();
			}
		}
	}

	private String getInstructorNames(String ids) {
		String instructors = "";
		ids = ids.replace('[', ' ').replace(']', ' ');
		for (int i = 1; i < allTeachers.size(); i++) {
			String[] instructorIds = ids.split(",");
			for (int j = 0; j < instructorIds.length; j++) {
				if (allTeachers.get(i).get("StaffID").equals(instructorIds[j].trim())) {
					if (instructors.length() != 0) {
						instructors += "\" and \"";
					}
					instructors += allTeachers.get(i).get("Name");
				}
			}
			
		}
		return instructors;
	}

	
	private String getCourseTotal(String id) {
		for (int i = 1; i < allCourses.size(); i++) {
			if (allCourses.get(i).get("ID").equals(id)) {
				return allCourses.get(i).get("TotalMarks");
			}
		}
		return "";
	}

	private String getCourseName(String id) {
		for (int i = 1; i < allCourses.size(); i++) {
			if (allCourses.get(i).get("ID").equals(id)) {
				return allCourses.get(i).get("Title");
			}
		}
		return "";
	}

	public HashMap<Integer, HashMap<String, String>> getResults(String studentId) {

		HashMap<Integer, HashMap<String, String>> currentResults = new HashMap<>();
		String currentStudentID = "";
		for (int i = 0; i < allExamResults.size(); i++) {
			if (allExamResults.get(i).get("StudentID") == null) {
				if (studentId.equals(currentStudentID)) {
					currentResults.put(currentResults.size(), allExamResults.get(i));
				}
			} else {
				currentStudentID = allExamResults.get(i).get("StudentID");
			}
		}
		return currentResults;
	}
	
	public void readInputFile(String filepath) {
		try {
			FileReader fr = new FileReader(filepath);
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(fr);
			for (String line; (line = reader.readLine()) != null;) {
				int spaceCount = line.length() - line.trim().length();
				doParse(line.trim(), spaceCount);
			}
			putCurrentEntityInCurrentMap(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void generateReportFile(String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			doEvaluateAndWrite(out);

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Report reportA = new Report();
		reportA.readInputFile("/Users/wajahat/eclipse-workspace/parsers/TestExcercise.txt");
		reportA.generateReportFile("TestReport.txt");
		System.out.println("Report Generated successfully!!");

	}

}

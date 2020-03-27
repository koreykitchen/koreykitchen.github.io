package main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import oracle.jdbc.OracleTypes;
import oracle.jdbc.pool.OracleDataSource;

public class Main 
{
	//Main Function - Used as text menu interface
	public static void main(String[] args) throws SQLException 
	{	
		//Create a way to get user input
		int input;
		Scanner scanner = new Scanner(System.in);
		
		//Output Ooptions
		System.out.println("\n1.)  Output a Table");
		System.out.println("2.)  Add a Student");
		System.out.println("3.)  Display a Student's Info");
		System.out.println("4.)  Show Course Pre-Req Info");
		System.out.println("5.)  Show Class Info");
		System.out.println("6.)  Enroll Student in Class");
		System.out.println("7.)  Drop Student from Class");
		System.out.println("8.)  Delete Student Data");
		System.out.println("0.)  EXIT");
		System.out.println("\nEnter a number: ");
		
		//Input user selection
		input = scanner.nextInt();
		
		//Keep allowing the user to select until they want to exit
		while (input != 0)
		{
			//Call the corresponding function based on the user input
			switch(input)
			{
				case 1:
					selectTableToOutput();
					break;
				
				case 2:
					addStudent();
					break;
				
				case 3:
					showStudentInfo();
					break;
					
				case 4:
					showCoursePrereqs();
					break;
				
				case 5:
					showClassInfo();
					break;
					
				case 6:
					enrollStudent();
					break;
					
				case 7:
					dropStudent();
					break;
					
				case 8:
					deleteStudent();
					break;
					
				default:
					break;
			}
			
			//Output the options for the user again after the previous request has been fulfilled
			System.out.println("\n1.)  Output a Table");
			System.out.println("2.)  Add a Student");
			System.out.println("3.)  Display a Student's Info");
			System.out.println("4.)  Show Course Pre-Req Info");
			System.out.println("5.)  Show Class Info");
			System.out.println("6.)  Enroll Student in Class");
			System.out.println("7.)  Drop Student from Class");
			System.out.println("8.)  Delete Student Data");
			System.out.println("0.)  EXIT");
			System.out.println("\nEnter a number: ");
			
			//Input next user request
			input = scanner.nextInt();
		
		}
		
		//Free resources
		scanner.close();
	}

	//SelectTableToOutput Function:
	//Used as another text menu to allow the user to select which table they want to output
	public static void selectTableToOutput() throws SQLException
	{
		//Setup a way to get user input
		int input;
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		//Output the options for the user to select from
		System.out.println("\n1.)  Students Table");
		System.out.println("2.)  Courses Table");
		System.out.println("3.)  Prerequisites Table");
		System.out.println("4.)  Classes Table");
		System.out.println("5.)  Enrollments Table");
		System.out.println("6.)  Logs Table");
		System.out.println("\nEnter a number: ");
		input = scanner.nextInt();
		
		//Select which additional function to call, based on the user input
		switch(input)
		{
			case 1:
				showStudents();
				break;
				
			case 2:
				showCourses();
				break;
				
			case 3:
				showPrerequisites();
				break;
				
			case 4:
				showClasses();
				break;
				
			case 5:
				showEnrollments();
				break;
				
			case 6:
				showLogs();
				break;
				
			default:
				break;
		}
	}
	
	
	//ShowStudents Function:
	//Connects to the oracle database using PL/SQL "showstudents" function
	//Uses a cursor to output all of the results from the database
	public static void showStudents() throws SQLException
	{
		//Setup the database connection
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getstudents(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3) + "\t" +
								rs.getString(4) + "\t" +
								rs.getDouble(5) + "\t" +
								rs.getString(6));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}
	
	
	//ShowCourses Function:
	//Connects to the oracle database using PL/SQL "showcourses" function
	//Uses a cursor to output all of the results from the database
	public static void showCourses() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getcourses(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}
	
	
	//ShowPrerequisites Function:
	//Connects to the oracle database using PL/SQL "showprerequisites" function
	//Uses a cursor to output all of the results from the database
	public static void showPrerequisites() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getprerequisites(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3) + "\t" +
								rs.getString(4));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}
	
	
	//ShowClasses Function:
	//Connects to the oracle database using PL/SQL "showclasses" function
	//Uses a cursor to output all of the results from the database
	public static void showClasses() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getclasses(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3) + "\t" +
								rs.getString(4) + "\t" +
								rs.getString(5) + "\t" +
								rs.getString(6) + "\t" +
								rs.getString(7) + "\t" +
								rs.getString(8));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}
	
	
	//ShowEnrollments Function:
	//Connects to the oracle database using PL/SQL "showenrollments" function
	//Uses a cursor to output all of the results from the database
	public static void showEnrollments() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getenrollments(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}
	
	
	//ShowLogs Function:
	//Connects to the oracle database using PL/SQL "showlogs" function
	//Uses a cursor to output all of the results from the database
	public static void showLogs() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Prepare to call stored procedure:
		CallableStatement cs = conn.prepareCall("begin ? := project2.getlogs(); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);

		// execute and retrieve the result set
		cs.execute();
		ResultSet rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(2) + "\t" + 
								rs.getString(3) + "\t" +
								rs.getString(4) + "\t" +
								rs.getString(5) + "\t" +
								rs.getString(6));
		}
		
		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}

	
	//AddStudent Function:
	//Prompts for student information, then uses this data as parameters to the database
	//Connects to the oracle database using PL/SQL "addstudent" function
	public static void addStudent() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Set up a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String sid;
		String firstName;
		String lastName;
		String status;
		String gpa;
		String email;
		
		//Prompt for all needed student data
		System.out.println("\nSid: ");
		sid = scanner.nextLine();
		
		System.out.println("First Name: ");
		firstName = scanner.nextLine();
		
		System.out.println("Last Name: ");
		lastName = scanner.nextLine();
		
		System.out.println("Status: ");
		status = scanner.nextLine();
		
		System.out.println("GPA: ");
		gpa = scanner.nextLine();
		
		System.out.println("Email: ");
		email = scanner.nextLine();
		
		//Setup a call to the database
		CallableStatement cs = conn.prepareCall("begin project2.addstudent(?,?,?,?,?,?); end;");
		
		cs.setString(1, sid);
		cs.setString(2, firstName);
		cs.setString(3, lastName);
		cs.setString(4, status);
		cs.setString(5, gpa);
		cs.setString(6, email);

		//Execute adding the student to the database
		cs.execute();
		
		//Inform user of success
		System.out.println("\nStudent successfully added...");
		
		//Free Resources
		cs.close();
		conn.close();
	}

	
	//ShowStudentInfo:
	//Prompts for a sid, then uses the sid as a parameter to access the database
	//Connects to the oracle database using PL/SQL "showstudentinfo" function
	//Uses a cursor to output all of the results from the database
	public static void showStudentInfo() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String sid;
		
		//Prompt for sid
		System.out.println("\nSid: ");
		sid = scanner.nextLine();
		
		// Setup query to see if sid is valid
		Statement stmt = conn.createStatement (); 

		//Try to select sid from students table
		ResultSet rset;
		rset = stmt.executeQuery ("SELECT * FROM students where sid='" + sid + "'");
		
		//If student is not in the database, then report error
		if(!rset.next())
		{
			System.out.println("\nThe sid is invalid...");
			
			//Free resources
			rset.close();
			stmt.close();
			conn.close();
			
			return;
		}
		
		//Print out basic student info
		System.out.println("\n" +	rset.getString(1) + "\t" +
									rset.getString(3) + "\t" +
									rset.getString(4) + "\n");
									
		//Setup the call to the database
		CallableStatement cs = conn.prepareCall("begin ? := project2.showstudentinfo(?); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);
		
		//set the in parameter (the second parameter) 
		cs.setString(2, sid);
		
		// execute and retrieve the result set
		cs.execute();
		
		ResultSet rs = (ResultSet)cs.getObject(1);
		
		//Used to tell if the student is taking any courses
		Boolean takenCourse = false;

		//Print the courses that the student is taking
		while (rs.next()) 
		{
			//Confirms student is taking at least one course
			takenCourse = true;
			
			//Print out all of the course info about the student
			System.out.println( rs.getString(9) + "\t" + 
								rs.getString(7) +
								rs.getString(8) + "\t" + 
								rs.getString(16) + "\t" +
								rs.getString(12) + "\t" + 
								rs.getString(13));
		}
		
		//If student has not taken a course, output a message saying so
		if(!takenCourse)
		{
			System.out.println("\nThe student has not taken any course...");
		}

		//Free Resources
		rset.close();
		stmt.close();
		rs.close();
		cs.close();
		conn.close();

	}
	

	//ShowCoursePrereqs:
	//Prompts for the course you want to know the prereqs for
	//Connects to the database using PL/SQL "showcourseprereqs" function
	//Uses a cursor to output all of the results from the database
	//Uses a queue to list all indirect prerequisite courses
	public static void showCoursePrereqs() throws SQLException
	{
		//Setup queues
		Queue<String> q1 = new PriorityQueue<String>();
		Queue<String> q2 = new PriorityQueue<String>();
		
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String deptCode;
		String courseNum;
		
		//Prompt for dept_code and course_no, add them to respective queues
		System.out.println("\nDepartment Code: ");
		deptCode = scanner.nextLine();
		q1.add(deptCode);
		
		System.out.println("Course Number: ");
		courseNum = scanner.nextLine();
		q2.add(courseNum);
		
		CallableStatement cs = null;
		ResultSet rs = null;
		
		//while there are courses to get prerequisites for in the queue
		while(!q1.isEmpty()) 
		{
		//Setup the call to the database	
		cs = conn.prepareCall("begin ? := project2.showcourseprereqs(?,?); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);
		
		//set the in parameters
		cs.setString(2, q1.peek());
		
		cs.setString(3, q2.peek());
		
		//Remove the just used course info from the queue
		q1.remove();
		q2.remove();
		
		// execute and retrieve the result set
		cs.execute();
		
		rs = (ResultSet)cs.getObject(1);

		// print the results
		while (rs.next()) 
		{
			System.out.println(	rs.getString(1) +
								rs.getString(2));
			
			//Add any additional indirect prerequisites to the queue
			q1.add(rs.getString(1));
			q2.add(rs.getString(2));
		}
		}

		//Free Resources
		rs.close();
		cs.close();
		conn.close();
	}

	
	//ShowClassInfo Function:
	//Prompts for the classid that info is wanted on
	//Connects to the database using PL/SQL "showclassinfo" function
	//Uses a cursor to output all data returned from the database
	public static void showClassInfo() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String classid;
		
		//Prompt for the required class id
		System.out.println("\nClass Id: ");
		classid = scanner.nextLine();
		
		// Setup query to see if classid is valid
		Statement stmt = conn.createStatement (); 

		//Try to select classid from classes table
		ResultSet rset;
		rset = stmt.executeQuery ("SELECT * FROM classes natural join courses where classid='" + classid + "'");
				
		//If the class is not in the database, then report error
		if(!rset.next())
		{
			System.out.println("\nThe cid is invalid...");
					
			//Free resources
			rset.close();
			stmt.close();
			conn.close();
					
			return;
		}
		
		//Print out basic class info
		System.out.println("\n" +	rset.getString(3) + "\t" +
									rset.getString(9) + "\t" +
									rset.getString(6) + "\t" +
									rset.getString(5) + "\n");
		
		//Setup the call to the database
		CallableStatement cs = conn.prepareCall("begin ? := project2.showclassinfo(?); end;");
		
		//register the out parameter (the first parameter)
		cs.registerOutParameter(1, OracleTypes.CURSOR);
		
		//set the in parameter (the second parameter) 
		cs.setString(2, classid);
		
		// execute and retrieve the result set
		cs.execute();
		
		ResultSet rs = (ResultSet)cs.getObject(1);
		
		//Used to tell if any student is taking the class
		Boolean studentsTaking = false;

		// print the results
		while (rs.next()) 
		{
			//Confirms a student is taking the class
			studentsTaking = true;
	
			//Print student info about students that are in the class
			System.out.println(	rs.getString(1) + "\t" +
								rs.getString(13));
		}
		
		//If no student is taking the class, output message saying so
		if(!studentsTaking)
		{
			System.out.println("\nNo student is enrolled in the class...");
		}

		//Free Resources
		rset.close();
		stmt.close();
		rs.close();
		cs.close();
		conn.close();
	}

	
	//EnrollStudent Function:
	//Prompts for sid and class id
	//Connects to the database using PL/SQL "enrollstudent" function
	public static void enrollStudent() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way tyo get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String sid;
		String classid;
		
		//Prompt for sid and classid
		System.out.println("\nSid: ");
		sid = scanner.nextLine();
		
		//--------------------------Check Sid----------------------------------
		// Setup query to see if sid is valid
		Statement stmt = conn.createStatement (); 

		//Try to select sid from students table
		ResultSet rset;
		rset = stmt.executeQuery ("SELECT * FROM students where sid='" + sid + "'");
				
		//If student is not in the database, then report error
		if(!rset.next())
		{
			System.out.println("\nThe sid is invalid...");
					
			//Free resources
			rset.close();
			stmt.close();
			conn.close();
					
			return;
		}
		
		//Free Resources
		rset.close();
		stmt.close();
		//---------------------------------------------------------------------
		
		System.out.println("Class Id: ");
		classid= scanner.nextLine();
		
		//-----------------------Check ClassID---------------------------------
		// Setup query to see if classid is valid
		Statement stmt2 = conn.createStatement (); 

		//Try to select classid from classes table
		ResultSet rset2 = stmt2.executeQuery ("SELECT * FROM classes where classid='" + classid + "'");
				
		//If the class is not in the database, then report error
		if(!rset2.next())
		{
			System.out.println("\nThe cid is invalid...");
					
			//Free resources
			rset2.close();
			stmt2.close();
			conn.close();
					
			return;
		}
		
		//Free Resources
		rset2.close();
		stmt2.close();
		//---------------------------------------------------------------------
		
		//----------------Check if there is room in class----------------------
		// Setup query to see if there is room in the class
		Statement stmt3 = conn.createStatement (); 

		//Select the size and limit from the database
		ResultSet rset3 = stmt3.executeQuery ("SELECT class_size, limit FROM classes where classid='" + classid + "'");
						
		//Look at the results
		rset3.next();
		
		//If there is no room, throw error and report message
		if(rset3.getInt(1) == rset3.getInt(2))
		{
			System.out.println("\nThe class is closed...");
							
			//Free resources
			rset3.close();
			stmt3.close();
			conn.close();
							
			return;
		}
			
		//Free Resources
		rset3.close();
		stmt3.close();
		//---------------------------------------------------------------------
		
		//----------------Check if student is already in class-----------------
		// Setup query to see if the student is alreday in the class
		Statement stmt4 = conn.createStatement (); 

		//Check the database to see if the student is already enrolled
		ResultSet rset4 = stmt4.executeQuery ("SELECT * FROM enrollments where classid='" + classid 
												+ "' and sid='" + sid +"'");

		//If there is a result, student is already in class, report error
		if(rset4.next())
		{
			System.out.println("\nThe student is already in the class...");

			//Free resources
			rset4.close();
			stmt4.close();
			conn.close();

			return;
		}

		//Free Resources
		rset4.close();
		stmt4.close();
		//---------------------------------------------------------------------
		
		//----------------Check if student has the prereq classes--------------
		//Setup query to get the class dept_code and course_no from the database
		Statement stmt8 = conn.createStatement();
		
		//Get dept_code and course_no form the database
		ResultSet rset8 = stmt8.executeQuery("SELECT dept_code, course_no FROM classes where classid='" 
												+ classid + "'");
		
		//Get result
		rset8.next();
		
		//Store the results for dept_code and course_no
		String dept_code = rset8.getString(1);
		String course_no = rset8.getString(2);
		
		//Free resources
		rset8.close();
		stmt8.close();
		
		// Setup query to get the prereqs for the course the student is trying to enroll in
		Statement stmt7 = conn.createStatement (); 

		//Get the prereqs from the database
		ResultSet rset7 = stmt7.executeQuery ("SELECT pre_dept_code, pre_course_no FROM prerequisites where dept_code='" 
												+ dept_code + "' and course_no=" + course_no);

		//Setup stuff for prereq checking
		Statement stmt10 = conn.createStatement();
		ResultSet rset10 = null;
		Boolean passed = false;
		
		//For all the prereqs, check whether the student has done them with grade C or better
		while(rset7.next())
		{
			rset10 = stmt10.executeQuery("SELECT lgrade from enrollments natural join classes where sid='"
											+ sid + "' and dept_code='" + rset7.getString(1) + "' and course_no="
											+ rset7.getString(2));
			
			//Reset passed to false
			passed = false;
			
			//For all times the student has taken the course, check the grade
			while(rset10.next())
			{
				if(rset10.getString(1) == "A" || rset10.getString(1) == "B" || rset10.getString(1) == "C")
				{
					passed = true;
				}
			}
			
			if(!passed)
			{
				System.out.println("\nPrerequisite courses have not been completed...");

				//Free resources
				rset7.close();
				stmt7.close();
				rset10.close();
				stmt10.close();
				conn.close();

				return;
			}
		}

		//Free Resources
		rset7.close();
		stmt7.close();
		if(rset10 != null)
		{
			rset10.close();
		}
		stmt10.close();
		//---------------------------------------------------------------------
		
		//----------------Check if student is taking 2 or 3 classes------------
		// Setup query to get semester and year
		Statement stmt5 = conn.createStatement (); 

		//Select year and semester from classes table
		ResultSet rset5 = stmt5.executeQuery ("SELECT year, semester FROM classes where classid='" + classid + "'");
		
		//Get results
		rset5.next();

		//Store results
		String semester = rset5.getString(2);
		int year = rset5.getInt(1);
		
		//Free Resources
		rset5.close();
		stmt5.close();
		
		// Setup query to get all classes a student is enrolled in
		Statement stmt6 = conn.createStatement (); 

		//Select results
		ResultSet rset6 = stmt6.executeQuery ("SELECT * FROM enrollments natural join classes where classid='" 
												+ classid + "' and year=" + year + " and semester='" 
												+ semester + "' and sid='" + sid + "'");
		
		//Setup counter to see how many classes the student is taking in the same year and semester
		int count = 0;

		//Check all the database reults, to get a count
		while(rset6.next())
		{
			count++;
			
			//If a student is alreday enrolled for three classes in the same semester and year
			//Report an error message and don't allow enrollment
			if(count == 3)
			{
				System.out.println("Students cannot be enrolled in more than three classes in the same semester...");
				
				//Free resources
				rset6.close();
				stmt6.close();
				
				return;
			}
		}
		
		//If the student is in two classes already for the same year and semester
		//Then report a message that they are now overloaded
		if(count == 2)
		{
			System.out.println("You are overloaded.");
		}
		
		//Free Resources
		rset6.close();
		stmt6.close();
		//---------------------------------------------------------------------
		
		//Setup the call to the database
		CallableStatement cs = conn.prepareCall("begin project2.enrollstudent(?,?); end;");
		
		cs.setString(1, sid);
		
		cs.setString(2, classid);
		
		//Execute the call
		cs.execute();

		//Print success message to the user
		System.out.println("\nStudent enrolled successfully...");

		//Free Resources
		cs.close();
		conn.close();
	}

	
	//DropStudent Function:
	//Prompts for sid and class id
	//Connects to the database using PL/SQL "dropstudent" function
	public static void dropStudent() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String sid;
		String classid;
		
		//Prompt for sid and classid
		System.out.println("\nSid: ");
		sid = scanner.nextLine();
		
		//--------------------------Check Sid----------------------------------
		// Setup query to see if sid is valid
		Statement stmt = conn.createStatement (); 

		//Try to select sid from students table
		ResultSet rset;
		rset = stmt.executeQuery ("SELECT * FROM students where sid='" + sid + "'");

		//If student is not in the database, then report error
		if(!rset.next())
		{
			System.out.println("\nThe sid is invalid...");

			//Free resources
			rset.close();
			stmt.close();
			conn.close();

			return;
		}

		//Free Resources
		rset.close();
		stmt.close();
		//---------------------------------------------------------------------

		System.out.println("Class Id: ");
		classid= scanner.nextLine();

		//-----------------------Check ClassID---------------------------------
		// Setup query to see if classid is valid
		Statement stmt2 = conn.createStatement (); 

		//Try to select classid from classes table
		ResultSet rset2 = stmt2.executeQuery ("SELECT * FROM classes where classid='" + classid + "'");

		//If the class is not in the database, then report error
		if(!rset2.next())
		{
			System.out.println("\nThe cid is invalid...");

			//Free resources
			rset2.close();
			stmt2.close();
			conn.close();

			return;
		}

		//Free Resources
		rset2.close();
		stmt2.close();
		//---------------------------------------------------------------------
		
		//----------------Check if student is actually in class----------------
		// Setup query to see if the student is  in the class
		Statement stmt4 = conn.createStatement (); 

		//Check the database to see if the student is already enrolled
		ResultSet rset4 = stmt4.executeQuery ("SELECT * FROM enrollments where classid='" + classid 
				+ "' and sid='" + sid +"'");

		//If there isn't a result, student is not in class, report error
		if(!rset4.next())
		{
			System.out.println("\nThe student is not enrolled in the class...");

			//Free resources
			rset4.close();
			stmt4.close();
			conn.close();

			return;
		}

		//Free Resources
		rset4.close();
		stmt4.close();
		//---------------------------------------------------------------------
		
		//----------------Check the prereq requirements------------------------
		// Setup query to get dept_code and course_no for the class to drop
		Statement stmt10 = conn.createStatement (); 

		//Get the dept_code and course_no from the database
		ResultSet rset10 = stmt10.executeQuery ("SELECT dept_code, course_no FROM classes where classid='" + classid + "'");
		
		//Get results
		rset10.next();
		
		//Store the resulting dept_code and course_no
		String dept_code = rset10.getString(1);
		String course_no = rset10.getString(2);
		
		//Free resources
		rset10.close();
		stmt10.close();
		
		//Setup query to get all courses where the dropping course is a prereq
		Statement stmt11 = conn.createStatement();
		
		//Get the results form the database
		ResultSet rset11 = stmt11.executeQuery("SELECT dept_code, course_no FROM prerequisites where pre_dept_code='"
												+ dept_code + "' and pre_course_no=" + course_no);
		
		//Setup things to check prereqs
		Statement stmt12 = conn.createStatement();
		ResultSet rset12 = null;

		//Check if the student is taking any classes that would not allow the drop
		while(rset11.next())
		{
			rset12 = stmt12.executeQuery("SELECT * FROM enrollments natural join classes where sid='"
											+ sid + "' and dept_code='" + rset11.getString(1) 
											+ "' and course_no=" + rset11.getString(2));
			
			//If there is a result, the student cannot drop
			if(rset12.next())
			{
				System.out.println("\nThe drop is not permitted because another class uses it as a prerequisite...");

				//Free resources
				rset11.close();
				stmt11.close();
				rset12.close();
				stmt12.close();
				conn.close();

				return;
			}
		}

		//Free Resources
		rset11.close();
		stmt11.close();
		if(rset12 != null)
		{
			rset12.close();
		}
		stmt12.close();
		//---------------------------------------------------------------------
		
		//Setup the call to the database
		CallableStatement cs = conn.prepareCall("begin project2.dropstudent(?,?); end;");
		
		cs.setString(1, sid);
		
		cs.setString(2, classid);
		
		//Execute the call
		cs.execute();

		//Print success message to the user
		System.out.println("\nStudent dropped successfully...");

		//Free Resources
		cs.close();
		
		//----------------Check if it was the students last class--------------
		// Setup query to see if the student has any more classes
		Statement stmt5 = conn.createStatement (); 

		//Check the database to see if the student has any more classes
		ResultSet rset5 = stmt5.executeQuery ("SELECT * FROM enrollments where sid='" + sid +"'");

		//If there isn't a result, student has no more classes, report message
		if(!rset5.next())
		{
			System.out.println("\nThis student is not enrolled in any classes...");

			//Free resources
			rset5.close();
			stmt5.close();
			conn.close();

			return;
		}

		//Free Resources
		rset5.close();
		stmt5.close();
		//---------------------------------------------------------------------
		
		//----------------Check if the class is now empty----------------------
		// Setup query to see if the class is empty
		Statement stmt6 = conn.createStatement (); 

		//Check the database to see if the class size is 0
		ResultSet rset6 = stmt6.executeQuery ("SELECT class_size FROM classes where classid='" + classid +"'");

		//Check the result
		rset6.next();
		
		//If the class size is zero, report the message
		if(rset6.getInt(1) == 0)
		{
			System.out.println("\nThe class now has no students...");

			//Free resources
			rset6.close();
			stmt6.close();
			conn.close();

			return;
		}

		//Free Resources
		rset6.close();
		stmt6.close();
		//---------------------------------------------------------------------
		
		//Close connection
		conn.close();
	}

	
	//DropStudent Function:
	//Prompts for sid
	//Connects to the database using PL/SQL "deletestudent" function
	public static void deleteStudent() throws SQLException
	{
		//Connection to Oracle server
		OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
		ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
		Connection conn = ds.getConnection("kkitche1", "***PASSWORD_REDACTED***");
		
		//Setup a way to get user input
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		String sid;
		
		//Prompt for sid
		System.out.println("\nSid: ");
		sid = scanner.nextLine();
		
		// Setup query to see if sid is valid
		Statement stmt = conn.createStatement (); 

		//Try to select sid from students table
		ResultSet rset;
		rset = stmt.executeQuery ("SELECT * FROM students where sid='" + sid + "'");
				
		//If student is not in the database, then report error
		if(!rset.next())
		{
			System.out.println("\nThe sid is invalid...");
					
			//Free resources
			rset.close();
			stmt.close();
			conn.close();
					
			return;
		}
		
		//Setup the call to the database
		CallableStatement cs = conn.prepareCall("begin project2.deletestudent(?); end;");
		
		cs.setString(1, sid);
		
		//Execute the call
		cs.execute();

		//Print out success message to the user
		System.out.println("\nStudent deleted successfully...");

		//Free Resources
		cs.close();
		conn.close();
	}
}

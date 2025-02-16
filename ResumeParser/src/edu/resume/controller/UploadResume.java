package edu.resume.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import edu.resume.dao.JobseekerDAO;
import edu.resume.entity.ResumeConstants;
import edu.resume.model.ProcessResume;

/**
 * Servlet implementation class UploadResume
 */
@WebServlet("/UploadResume")
@MultipartConfig
public class UploadResume extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadResume() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String email = request.getParameter("email");
		System.out.println("Email:" + email);

		int jid = JobseekerDAO.getJidFromEmail(email);
		if (jid > -1) {

			Part filePart = request.getPart("resumeFile"); // Retrieves <input type="file" name="file">
			String resumeFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
			InputStream uploadStream = filePart.getInputStream();
			File resumeFolder = new File(ResumeConstants.UPLOAD_DIRECTORY + File.separator + jid);
			if (resumeFolder.exists()) {
				deleteResumes(resumeFolder);
			} else {
				resumeFolder.mkdirs();
			}

			System.out.println("resumeFileName: " + resumeFileName);
			String completePath = ResumeConstants.UPLOAD_DIRECTORY + File.separator + jid + File.separator + resumeFileName;
			Path path = Paths.get(completePath);
			System.out.println("path:" + path);
			Files.copy(uploadStream, path);
			uploadStream.close();
			InputStream resumeStream = null;
			try {
				File file = new File(completePath);
				resumeStream = new FileInputStream(file);
				String resumeFileType = Files.probeContentType(file.toPath());

				ProcessResume processor = new ProcessResume(file, resumeStream,resumeFileType, email, jid);
				processor.resumeFileProces();
			} finally {
				if (resumeStream != null)
					resumeStream.close();
			}
		}

	}

	//delete already present resumes
	private void deleteResumes(File resumeFolder) {

		if(resumeFolder.isDirectory() == false) {
			System.out.println("Not a directory. Do nothing");
			return;
		}
		File[] listFiles = resumeFolder.listFiles();
		for(File file : listFiles){
			System.out.println("Deleting "+file.getName());
			file.delete();
		}
	}

}

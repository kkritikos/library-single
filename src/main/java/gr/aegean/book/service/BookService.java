package gr.aegean.book.service;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;

import gr.aegean.book.domain.Book;
import gr.aegean.book.exception.InternalServerErrorException;
import gr.aegean.book.utility.DBHandler;
import gr.aegean.book.utility.HTMLHandler;
import spark.Request;
import spark.Response;

public class BookService {
	
	private static boolean jsonContentType(String type) {
		if (type != null && !type.trim().equals("") && type.equals("application/json")) return true;
		else return false;
	}
	
	private static boolean authenticate(Request req) throws InternalServerErrorException{
		boolean authenticated = false;
		String auth = req.headers("Authorization");
		if(auth != null && auth.startsWith("Basic")) {
			String b64Credentials = auth.substring("Basic".length()).trim();
			String credentials = new String(Base64.getDecoder().decode(b64Credentials));
			System.out.println(credentials);
			String[] creds = credentials.split(":");
			authenticated = DBHandler.existsUser(creds[0],creds[1]);
		}

		return authenticated;
	}
	
	private static void deleteBook() {
		delete("/api/books/:isbn", (req, res) -> {		
			try {
				if (!authenticate(req)) {
					res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
					res.status(401);
					return "Not Authenticated";
				}
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			String isbn = req.params(":isbn");
			
			boolean deleted = false;
			
			try {
				deleted = DBHandler.deleteBook(isbn);
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			if (deleted) {
				res.status(204);
				return "";
			}
			else {
				res.status(404);
				return "The requested resource was not found";
			}
		});
	}
	
	private static void putBook() {
		put("/api/books/:isbn", (req, res) -> {
			try {
				if (!authenticate(req)) {
					res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
					res.status(401);
					return "Not Authenticated";
				}
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			if (!jsonContentType(req.contentType())) {
				res.status(415);
				return "Only accept JSON as content type";
			}
			
			String body = req.body();
			Book book = null;
			if (body == null || body.trim().equals("")) {
				res.status(400);
				return "The request does not have any content";
			}
			else {
				try {
					book = new Gson().fromJson(body,Book.class);
				}
				catch(Exception e) {
					res.status(400);
					return "Did not provide a proper JSON content for the posted book";
				}
			}
			
			String bookIsbn = book.getIsbn();
			String isbn = req.params(":isbn");
			if (bookIsbn == null || bookIsbn.trim().equals("")) {
				res.status(400);
				return "Did not provide an isbn in the supplied book";
			}
			else if (!bookIsbn.equals(isbn)) {
				res.status(400);
				return "The book's isbn does not match the isbn path parameter";
			}
				
			//Checking all obligatory fields in book apart from isbn
			List<String> authors = book.getAuthors();
			if (authors == null || authors.isEmpty()) {
				res.status(400);
				return "MUST provide the authors of the book";
			}
			String publisher = book.getPublisher();
			if (publisher == null || publisher.trim().equals("")) {
				res.status(400);
				return "MUST provide the publisher of the book";
			}
			String title = book.getTitle();
			if (title == null || title.trim().equals("")) {
				res.status(400);
				return "MUST provide the title of the book";
			}
			
			boolean updated = false;
				
			try {
				updated = DBHandler.updateBook(book);
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			if (!updated) {
				res.status(404);
				return "Requested resource was not found";
			}
			
			res.status(204);
			return "";
		});
	}
	
	private static void postBook() {
		post("/api/books", (req, res) -> {
			try {
				if (!authenticate(req)) {
					res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
					res.status(401);
					return "Not Authenticated";
				}
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			if (!jsonContentType(req.contentType())) {
				res.status(415);
				return "Only accept JSON as content type";
			}
			
			String body = req.body();
			Book book = null;
			if (body == null || body.trim().equals("")) {
				res.status(400);
				return "The request does not have any content";
			}
			else {
				try {
					book = new Gson().fromJson(body,Book.class);
				}
				catch(Exception e) {
					res.status(400);
					return "Did not provide a proper JSON content for the posted book";
				}
			}
				
			String bookIsbn = book.getIsbn();
			if (bookIsbn == null || bookIsbn.trim().equals("")) {
				res.status(400);
				return "Did not provide an isbn in the supplied book";
			}
				
			//Checking all obligatory fields in book apart from isbn
			List<String> authors = book.getAuthors();
			if (authors == null || authors.isEmpty()) {
				res.status(400);
				return "MUST provide the authors of the book";
			}
			String publisher = book.getPublisher();
			if (publisher == null || publisher.trim().equals("")) {
				res.status(400);
				return "MUST provide the publisher of the book";
			}
			String title = book.getTitle();
			if (title == null || title.trim().equals("")) {
				res.status(400);
				return "MUST provide the title of the book";
			}
				
			boolean exists = false;
			try {
				exists = DBHandler.existsBook(bookIsbn);
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
			
			if (exists) {
				res.status(400);
				return "Book with given isbn already exists";
			}
			else {
				try {
					DBHandler.createBook(book);
				}
				catch(Exception e) {
					res.status(500);
					return e.getMessage();
				}
			}
			
			res.status(201);
			return req.url() + '/' + book.getIsbn();
		});
	}
	
	private static void getBookHTML() {
		get("/api/books/:isbn", "text/html", (req, res) -> {
			return getBook(req,res,false);
		});
	}
	
	private static void getBookJSON() {
		get("/api/books/:isbn", "application/json", (req, res) -> {
			return getBook(req,res,true);
		});
	}
	
	private static String getBook(Request req, Response res, boolean isJSON) {
		String isbn = req.params(":isbn");
		Book book = null;
		try {
			book = DBHandler.getBook(isbn);
		}
		catch(Exception e) {
			res.status(500);
			return e.getMessage();
		}
		
		if (book != null) {
			res.status(200);
				
			if (isJSON) {
				res.type("application/json");
				return new Gson().toJson(book);
			}
			else {
				String answer = HTMLHandler.createHtmlBook(book);
				res.type("text/html");
				return answer;
			}
		}
		else {
			res.status(404);
			return "Requested resource was not found";
		}	
	}
	
	private static void getBooksJSON() {
		get("/api/books", "application/json", (req, res) -> {
			return getBooks(req,res,true);
		});
	}
	
	private static void getBooksHTML() {
		get("/api/books", "text/html", (req, res) -> {
			return getBooks(req,res,false);
		});
	}
	
	private static String getBooks(Request req, Response res, boolean isJSON) {
		String title = req.queryParams("title");
		String publisher = req.queryParams("publisher");
		String[] auths = req.queryParamsValues("authors");
		List<String> authors = null;
		if (auths != null && auths.length > 0) authors = Arrays.asList(auths);
		List<Book> books = null;
		if ((title != null && !title.trim().equals("")) || (authors != null && !authors.isEmpty()) || (publisher != null && !publisher.trim().equals(""))){
			try {
				books = DBHandler.getBooks(title,authors,publisher);
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
		}
		else {
			try {
				books = DBHandler.getAllBooks();
			}
			catch(Exception e) {
				res.status(500);
				return e.getMessage();
			}
		}
		
		if (isJSON) {
			res.status(200);
			res.type("application/json");
			return new Gson().toJson(books);
		}
		else {
			res.status(200);
			res.type("text/html");
			if (books == null) books = new ArrayList<Book>();
			String answer = HTMLHandler.createHtmlBooks(books);
			return answer;
		}
	}
	
	private static void run() {
		port(30000);
		staticFileLocation("public");
		getBooksJSON();
		getBooksHTML();
		getBookHTML();
		getBookJSON();
		postBook();
		putBook();
		deleteBook();
	}
	public static void main(String[] args) {
		run();
	}
}

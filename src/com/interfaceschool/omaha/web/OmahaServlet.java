package com.interfaceschool.omaha.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interfaceschool.omaha.pojo.City;

/**
 * Servlet implementation class OmahaServlet
 */
@WebServlet("/omaha")
public class OmahaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource ds;
	
    public OmahaServlet() throws NamingException {
        super();
        String jndiName = "jdbc/omaha";
        InitialContext ctx = new InitialContext();
        try {
        	ds = (DataSource) ctx.lookup("java:comp/env/" + jndiName);
        }
        catch (NamingException e) {
        	ds = (DataSource) ctx.lookup(jndiName);
        }
    }

    private List<City> getCities() {
    	List<City> cities = new LinkedList<City>();
    	Connection connection = null;
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try {
    		connection = ds.getConnection();
    		stmt = connection.prepareStatement("select * from omaha.city");
    		rs = stmt.executeQuery();
    		while (rs.next())
    		{
    			City city = new City();
    			city.setId(rs.getInt("city_id"));
    			city.setName(rs.getString("city_name"));
    			city.setPopulation(rs.getInt("city_population"));
    			cities.add(city);
    		}
    	}
    	catch (SQLException e) {
    		System.err.println(e);
    	}
    	finally {
    		closeResources(rs, stmt, connection);
    	}
    	return cities;
    }
    
    private static void closeResources(ResultSet rs, PreparedStatement stmt, Connection connection) {
		//clean up resources here
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
		}

	}
    
    private void getErrorMessage(String text, HttpServletResponse response) throws IOException {
		JSONObject msg = new JSONObject();
		if (text == null) {
			text = "We could not complete your request, please try again later";
		}
		msg.put("msg", text);
		response.getWriter().write(msg.toString());
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<City> cities = getCities();
		if (cities == null) {
			getErrorMessage(null, response);
			return;
		}
		
		JSONArray root = new JSONArray();
		for (City city : cities) {
			JSONObject cityObj = new JSONObject();
			cityObj.put("name", city.getName());
			cityObj.put("population", city.getPopulation());
			root.put(cityObj);
		}
		response.getWriter().write(root.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

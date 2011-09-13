import groovy.text.SimpleTemplateEngine

/**
 * Created by IntelliJ IDEA.
 * User: imranansari
 * Date: 9/12/11
 * Time: 8:16 PM
 * To change this template use File | Settings | File Templates.
 */

String.metaClass.normalize = {
    delegate.split("_").collect { word ->
        word.toLowerCase().capitalize()
    }.join("")
}

daoName = "User"
daoFolder = '/Users/imranansari/temp/lfg/';
packageName = 'com.lfg.pending'
sql = "SELECT first_name, last_name, user_id, role_id From User"


def createDAOParms() {
    def binding = [
            daoName: daoName,
            lowerDaoName: daoName.toLowerCase(),
            upperDaoName: daoName.toUpperCase(),
            className: daoName + 'DAO',
            packageName: packageName,
            methodName: 'get' + daoName,
            fields: getFieldsFromSql(sql),
            sql: sql
    ]

    return binding;
}

String[] getFieldsFromSql(sql){
    def selectFields = sql.substring(6, sql.indexOf("From"));
    //println(selectFields)
    selectFields = selectFields.replace(" ","");
    return selectFields.split(",")
}

String getDAOTemplate() {
    def springContextTemplate = '''
package $packageName;

public Class $className {

	/** The jdbc template. */
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String <%= upperDaoName %>_SQL = $sql;

	public List $methodName(){
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("key", "value");
		List resultList = jdbcTemplate.query(<%= upperDaoName %>_SQL, namedParameters, new <%= daoName %>Mapper());

    }


	static final class <%= daoName %>Mapper implements RowMapper<<%= daoName %>> {

		public <%= daoName %> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			<%= daoName %> <%= lowerDaoName %> = new <%= daoName %>();

            <% fields.each{ field -> %>
                 <%= lowerDaoName %>.set<%= field.normalize() %>(rs.getString("<%= field%>");
            <%}%>

			return <%= daoName %>;
		}
	}

}

'''

    return springContextTemplate;
}

def engine = new SimpleTemplateEngine()
def template = engine.createTemplate(getDAOTemplate()).make(createDAOParms())
new File(daoFolder, daoName + 'DAO.java').write(template.toString())
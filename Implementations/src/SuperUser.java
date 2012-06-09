import twitter4j.User;
import java.util.Date;

public class SuperUser implements java.io.Serializable{
	private static final long serialVersionUID = 8108420761604284142L;
	User user;
	Date lastUpdated;
	double weight;
	public SuperUser(User user, double weight, Date lastUpdated)
	{
		this.user = user;
		this.weight = weight;
		this.lastUpdated = lastUpdated;
	}
}

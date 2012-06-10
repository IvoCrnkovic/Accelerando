import twitter4j.User;
import java.util.Date;

public class SuperUser implements java.io.Serializable{
	private static final long serialVersionUID = 8108420761604284142L;
	private final User user;
	private Date lastUpdated;
	private double weight;
	public SuperUser(User user, double weight, Date lastUpdated)
	{
		this.user = user;
		this.weight = weight;
		this.lastUpdated = lastUpdated;
	}
	public User getUser()
	{
		return user;
	}
	public double getWeight()
	{
		return weight;
	}
	public Date getLastUpdated()
	{
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	public void setWeight(double weight)
	{
		this.weight = weight;
	}
}

package SpringDemoBot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "info")
public class Info {

	private long id;
	private long telegramId;
	private String city;

	
	public Info() {
		
	}

	public Info(long telegramId, String city) {
		this.telegramId = telegramId;
		this.city = city;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "telegramId", nullable = false)
	public long getTelegramId() {return telegramId;}
	public void setTelegramId(long city) {this.telegramId = telegramId;}

	@Column(name = "city", nullable = false)
	public String getCity() {return city;}
	public void setCity(String city) {this.city = city;}

	@Override
	public String toString() {
		return "Info{" +
				"id=" + id +
				", telegramId=" + telegramId +
				", city='" + city + '\'' +
				'}';
	}
}

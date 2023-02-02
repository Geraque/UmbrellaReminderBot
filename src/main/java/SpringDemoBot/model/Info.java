package SpringDemoBot.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "info")
public class Info {

	private long id;
	private long telegramId;
	private String city;
	private String time;

	
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


	@Override
	public String toString() {
		return "Info{" +
				"id=" + id +
				", telegramId=" + telegramId +
				", city='" + city + '\'' +
				", time='" + time + '\'' +
				'}';
	}
}

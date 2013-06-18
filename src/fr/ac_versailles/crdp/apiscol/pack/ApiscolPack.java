package fr.ac_versailles.crdp.apiscol.pack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import fr.ac_versailles.crdp.apiscol.database.DBAccessException;

public class ApiscolPack extends ServletContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApiscolPack() {

	}

	public ApiscolPack(Class<? extends Application> appClass) {
		super(appClass);
	}

	public ApiscolPack(Application app) {
		super(app);
	}

	@PreDestroy
	public void deinitialize() {
		PackApi.disconnectDb();

	}

	@PostConstruct
	public void initialize() {
		// nothing at this time
	}
}

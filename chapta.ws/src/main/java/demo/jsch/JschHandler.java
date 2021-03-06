package demo.jsch;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class JschHandler {

	private static final Logger log = LoggerFactory
			.getLogger(JschHandler.class);

	public static final String SFTP_PROTOCAL = "sftp";
	private String username;
	private String host;
	private int port;
	private String identity;
	private UserInfo userInfo;

	private JSch jsch = null;
	protected Session session = null;
	private boolean firstInit = false;
	private int authType = -1;

	/**
	 * Private/public key authorization
	 * 
	 * @param username
	 *            user account
	 * @param host
	 *            server host
	 * @param port
	 *            ssh port
	 * @param identity
	 *            the path of private key file.
	 * @see http://www.jcraft.com/jsch/
	 */
	public JschHandler(String username, String host, int port, String identity) {
		this.username = username;
		this.host = host;
		this.port = port;
		this.identity = identity;

		firstInit = false;
		jsch = new JSch();

		authType = 0;
	}

	/**
	 * Password authorization
	 * 
	 * @param username
	 * @param host
	 * @param port
	 * @param userInfo
	 *            User information for authorization
	 * @see com.jcraft.jsch.UserInfo
	 * @see http://www.jcraft.com/jsch/
	 */
	public JschHandler(String username, String host, int port, UserInfo userInfo) {
		this.username = username;
		this.host = host;
		this.port = port;
		this.userInfo = userInfo;

		firstInit = false;
		jsch = new JSch();
		authType = 1;
	}

	/**
	 * 
	 * Initialize SSH session. When the parameters is not right, It will throw
	 * an JSchException.
	 * 
	 * @throws MessageServicerException
	 * @see com.jcraft.jsch.JSch
	 */
	@SuppressWarnings("static-access")
	public void init() throws JSchException {
		try {
			validate();
			log.info("JSCH identity:" + identity);
			jsch.setLogger(new JschLogger());
			jsch.setConfig("StrictHostKeyChecking", "no");

			if (authType == 0)
				jsch.addIdentity(identity);
			session = jsch.getSession(username, host, port);

			if (authType == 1)
				session.setUserInfo(userInfo);
			// session.setPassword(userInfo.getPassphrase());
			session.connect();

			log.info("JSCH session connect success.");
		} catch (JSchException e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public void destory() {
		if (session != null)
			session.disconnect();
		log.info("JSCH session destory");
	}

	public ReturnCode exec(String cmd) throws Exception {

		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(cmd);
		channelExec.setInputStream(null);
		channelExec.setErrStream(System.err);
		InputStream in = channelExec.getInputStream();
		channelExec.connect();
		int res = -1;
		StringBuffer buf = new StringBuffer(4096);
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				buf.append(new String(tmp, 0, i));
			}
			if (channelExec.isClosed()) {
				res = channelExec.getExitStatus();
				System.out.println(String.format("Exit-status: %d", res));
				break;
			}
			// Wand.waitA( 100 );

		}//end of while
		System.out.println(buf.toString());
		channelExec.disconnect();
		
		return new ReturnCode(res, buf.toString());
	}

	/**
	 * Validate parameters
	 * 
	 * @throws JSchException
	 */
	private void validate() throws JSchException {

		if (firstInit)
			return;
		if (username == null || username.isEmpty()) {
			throw new JSchException("Parameter:username is empty.");
		}
		if (host == null || host.isEmpty()) {
			throw new JSchException("Parameter:host is empty.");
		} else {
			try {
				InetAddress inet = InetAddress.getByName(host);
				host = inet.getHostAddress();
				log.info("JSCH connection address:" + host);
			} catch (UnknownHostException e) {
				throw new JSchException(e.getMessage(), e);
			}
		}

		if (authType == 0 && (identity == null || identity.isEmpty())) {
			throw new JSchException("Parameter:identity is empty.");
		}

		if (authType == 1 && (userInfo == null)) {
			throw new JSchException("Parameter:userInfo is empty.");
		}

		firstInit = true;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
}

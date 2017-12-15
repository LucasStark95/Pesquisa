package Comunicacao;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

public class Receptor {

	public static void main(String[] args) throws IOException {
		ServerSocket welcomeSocket = new ServerSocket(3389);
		System.out.println("Iniciando servidor");

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			if (connectionSocket.isConnected()) {
				Thread novaRequisicao = new Requisicao(connectionSocket);
				novaRequisicao.start();
			}
		}
	}
}

class Requisicao extends Thread {

	private Socket s;
	private Socket servidorLua;
	private DataOutputStream dosLua;

	public Socket getS() {
		return s;
	}

	public void setS(Socket s) {
		this.s = s;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	private String nomeArquivo;

	public Requisicao(Socket s) throws IOException {
		this.s = s;
		this.servidorLua = new Socket("localhost", 8081);
		this.dosLua = new DataOutputStream(servidorLua.getOutputStream());

	}

	public void run() {
		OutputStream os = null;
		try {

			
				BufferedInputStream requisicaoDoCliente = new BufferedInputStream(this.s.getInputStream());
				System.out.println("Entrou no run");
				try {
					s.setSoTimeout(0);
					servidorLua.setKeepAlive(true);
					System.out.printf("Timeout atual: %d", 20000);
				} catch (Exception e) {
					s.close();

					System.out.println("Passou timeout");
					e.printStackTrace();
					return;
				}
				byte[] buffer = new byte[512];
				int read;

				System.out.println("Aguardando dados do arquivo");

				os = new BufferedOutputStream(new FileOutputStream(
						"/home/lucas/Documentos/TAIA/neuraltalk2/imgs/imagem.png"));

				while ((read = requisicaoDoCliente.read(buffer)) != -1) {

					System.out.println("Recebendo dados do servidor");
					// lerMensagemBytes(buffer);
					System.out.println("Gravando imagem em disco");
					os.write(buffer, 0, read);
				}

				// Thread.sleep(1000);
				if (servidorLua.isConnected()) {
                    String ipAdress = s.getInetAddress().getHostAddress();
					System.out.println("Enviando mensagem para Lua");
					dosLua.writeBytes(ipAdress + "\n");
					// dosLua.writeBytes("\n");
					System.out.println("Enviada");
				}

				 dosLua.flush();
			
			// dos.close();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				os.flush();
				os.close();
				s.close();
				dosLua.close();
				servidorLua.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void lerMensagemBytes(byte[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			System.out.print((char) buffer[i]);
		}
		System.out.println();
	}

	public byte[] lerBytes(String nomeArquivo) throws IOException {
		Path caminho = Paths.get("", new String[] { nomeArquivo });
		System.out.println(caminho);
		return Files.readAllBytes(caminho);
	}
}

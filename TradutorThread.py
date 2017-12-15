# coding: utf-8

import socket
import threading
import time
from selenium import webdriver

translatedDescription = '\n'
lock = threading.Lock()

class TradutorServer(object):

    def __init__(self, host='', port=3390):

        self.host = host
        self.port = port
        self.socketServer = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socketServer.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socketServer.bind((self.host, self.port))

        self.webOp = webdriver.Firefox()
        url = 'https://translate.google.com.br/'
        self.webOp.get(url)
        
    def startListenProcess(self):
        self.socketServer.listen(2)
        while True:
            con, address = self.socketServer.accept()
            print 'Iniciada conexao com o endereco ', address
            add = str(address)
            if "127.0.0.1" in add:
                con.settimeout(60)
                print 'Iniciando Thread para a conexao ', address
                threading.Thread(target=self.listenDL, args=(con, address)).start()
            else:
                threading.Thread(target=self.listenClient, args=(con, address)).start()
            
            print 'Mensagem traduzida: ', translatedDescription

    
    def listenDL(self, serverDL, address):

        global translatedDescription 
        global lock
        lock.acquire()
        
        messageFromDL = serverDL.recv(512)
        if not messageFromDL: return
        print 'Mensagem recebida do cliente ', address, ": ", messageFromDL + '\n'
        translatedDescription = self.translateMessage(messageFromDL)
        
        print 'Finalizando conexao'
        serverDL.close()
        lock.release()
        print 'Conexao finalizada'

    def translateMessage(self, message):
            url = 'https://translate.google.com.br/#en/pt/' + message.replace(' ', '%20')
            self.webOp.get(url)
	    self.webOp.refresh() 
            time.sleep(1)
            description = 'none'    
            for elem in self.webOp.find_elements_by_xpath('.//span[@id = "result_box"]'):
                description = elem.text
                #print 'Descricao traduzida: ', description

            return description
    
    def listenClient(self, client, address):
        global translatedDescription
        global lock

        lock.acquire()
        print 'Enviando descricao "%s" para client\n', translatedDescription
        description = translatedDescription + '\n'
        client.send(description.encode('utf-8'))
        print 'Descricao enviada para client\n'
        print 'Fechando conexao\n'
        client.close()
        lock.release()

if __name__ == "__main__":
    #host = input('Please, provide a host: ')
    #port = input('Please, provide a port: ')
    TradutorServer().startListenProcess()


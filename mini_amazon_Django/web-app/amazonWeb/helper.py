from socket import *
from .models import *
import math

serverName = '127.0.0.1'
packagePort = 11111
usernamePort = 22222


def sendToBackend(packageID):
    try:
        backend_socket = socket(AF_INET, SOCK_STREAM)
        backend_socket.connect((serverName, packagePort))
        msg = str(packageID)+'\n'
        backend_socket.send(msg.encode('utf-8'))
        response = backend_socket.recv(256)
        response = response.decode()
        components = response.split(' ')
        if components[0] == 'received' and components[1] == str(packageID):
            return True
        else:
            print('backend says: '+response)
            return False
    except ConnectionRefusedError:
        return False


def checkUPSuserinfo(userinfo):
    try:
        backend_socket = socket(AF_INET, SOCK_STREAM)
        backend_socket.connect((serverName, usernamePort))
        msg = str(userinfo)+'\n'
        backend_socket.send(msg.encode('utf-8'))
        response = backend_socket.recv(256)
        response = response.decode()
        components = response.split(' ')
        if components[0] == 'checking':
            return True
        else:
            print('backend says: '+response)
            return False
    except ConnectionRefusedError:
        return False

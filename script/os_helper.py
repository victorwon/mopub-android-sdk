import os
import subprocess

TEMP_STAGING_PATH = '/tmp/mopub-staging'

def try_system(command):
    popen = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, env=os.environ)
    stdout, stderr = popen.communicate()    
    if popen.returncode:
        print 'error:\n\t' + command + '\nfailed, exiting'
        print '"' + stderr + '"'
        print '"' + stdout + '"'
        exit(1)
    if len(stdout) > 0:
        print stdout
    return stdout

def try_system_quiet(command):
    with open(os.devnull, 'wb') as devnull:
        popen = subprocess.Popen(command, stdout=devnull, stderr=subprocess.STDOUT, shell=True, env=os.environ)
        popen.communicate()
    if popen.returncode:
        print 'error:\n\t' + command + '\nfailed, exiting'
        exit(1)

def try_chdir(path):
    if os.chdir(path):
        print 'error: couldn\'t chdir to' + path + ', exiting'
        exit(1)

def system_stdout(command):
    popen = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, env=os.environ)
    stdout, stderr = popen.communicate()
    return stdout

def system_quiet(command):
    with open(os.devnull, 'wb') as devnull:
        popen = subprocess.Popen(command, stdout=devnull, stderr=subprocess.STDOUT, shell=True, env=os.environ)
        popen.communicate()

def system_echo(command):
    print command
    return os.system(command)

def init_temp_staging() :
    print 'Cleaning staging path'
    os.system('rm -rf ' + TEMP_STAGING_PATH)
    print 'Creating staging path'
    os.system('mkdir -p ' + TEMP_STAGING_PATH)

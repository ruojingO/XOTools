# -*- coding: utf-8 -*-
import sys
sys.path.append('C:/Users/ruoji/AppData/Local/Packages/PythonSoftwareFoundation.Python.3.12_qbz5n2kfra8p0/LocalCache/local-packages/Python312/site-packages')
import socks
import socket
import traceback
import os
import io

# Set the standard output encoding to UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
# Set up the proxy
#socks.set_default_proxy(socks.SOCKS5, "127.0.0.1", 10808)

   # Configure the HTTP proxy
os.environ['HTTP_PROXY'] = 'http://127.0.0.1:10809'
os.environ['HTTPS_PROXY'] = 'http://127.0.0.1:10809'

# Patch the socket module
#socket.socket = socks.socksocket

# Assuming google.generativeai is a valid module and is installed
import google.generativeai as genai

# It's better to use environment variables for API keys
import os

# Check if an argument is provided
if len(sys.argv) > 1:
    #transE = '"""'+sys.argv[1]+'"""'
    transE = sys.argv[1]
    #print(transE)
else:
    transE = "Default value if no argument provided"
apikey = os.getenv('GOOGLE_GENAI_API_KEY')
genai.configure(api_key=apikey)

model = genai.GenerativeModel('gemini-pro')

# Removed %%time - use this only in Jupyter Notebooks

try:
    #up="What is the meaning of life?"
#     up=""" translate the tech english to chinese ,the text is :
#        The String class represents character strings. All string literals in Java programs, such as "abc", are implemented as instances of this class.
# Strings are constant; their values cannot be changed after they are created. String buffers support mutable strings. Because String objects are immutable they can be shared. For example:
#       String str = "abc";
#
# is equivalent to:
#       char data[] = {'a', 'b', 'c'};
#       String str = new String(data);
#     """
    response = model.generate_content(transE)
    # Assuming to_markdown is a defined function
    #print(to_markdown(response.text))
    result = response.text;
    #print(result.encode('utf-8'))
    print(result)
except Exception as e:
    #print("An error occurred: {0}".format(e))
    print(transE)
    traceback.print_exc()


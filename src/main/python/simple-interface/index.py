from flask import Flask,request,render_template,jsonify


app = Flask(__name__)

@app.route("/index")
def index():
    return jsonify("hello world")




if __name__ == '__main__':
    app.run(host='0.0.0.0',port=11111)
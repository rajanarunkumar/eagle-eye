import React, {Component} from 'react';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

var recordedBlobs = [];

class Webcam extends Component {
    constructor(props) {
        super(props);
        this.state = {
            mediaStream: '',
            mediaRecorder: '',
            image: '',
        };
        this.gotLocalMediaStream = this.gotLocalMediaStream.bind(this);
        this.handleLocalMediaStreamError = this.handleLocalMediaStreamError.bind(this);
        this.takePhoto = this.takePhoto.bind(this);
        this.checkPhotoDataExists = this.checkPhotoDataExists.bind(this);
        this.sendPhotoBlob = this.sendPhotoBlob.bind(this);
        this.handleDataAvailable = this.handleDataAvailable.bind(this);
        this.handleData = this.handleData.bind(this);
        this.connect = this.connect.bind(this);
    }

    componentDidMount() {
        this.setState({
            recorder: navigator.mediaDevices.getUserMedia({
                video: true,
            })
                .then(this.gotLocalMediaStream).catch(this.handleLocalMediaStreamError)
        });
        // this.connect();
        // setInterval(() => {this.setState({counter:this.state.counter +1}); this.takePhoto()},500)
    }

    handleData(data) {
        // TODO Recieve message and display image on screen
        console.log(data);
    }

    connect() {
        let stompClient;
        console.log("trying to connect");
        var socket = new SockJS('http://localhost:8080/eagle');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/greetings', function (greeting) {
                console.log((JSON.parse(greeting.body).content));
            });
        });
    }

    gotLocalMediaStream(mediaStream) {
        document.querySelector('video').srcObject = mediaStream;
        this.setState({mediaStream: mediaStream});
    }

    handleLocalMediaStreamError(error) {
        console.log('navigator.getUserMedia error: ', error);
    }

    takePhoto() {
        let video = document.querySelector('video');
        let photo = document.getElementById('photo');
        let photoContext = photo.getContext('2d');
        photoContext.drawImage(video, 0, 0, photo.width, photo.height);
        let image = photo.toDataURL('image/jpeg', 1.0);
        photo.setAttribute('src', image);
        fetch(image)
            .then(res => res.blob())
            .then(blob => this.setState({image: blob}));
        return image;
    }

    sendPhotoBlob(match)  {
        if(this.checkPhotoDataExists() === true){
            let url = "http://localhost:8080/image/match";
            if (match === false) {
                let sid = prompt("Please enter the user's SID:");
                if(sid.length !== 7) {
                    console.log("SID MUST BE 7 CHARACTERS");
                    return;
                }
                url = "http://localhost:8080/image/s3upload?name=" + sid
            }
            let form = new FormData();
            form.append("file", this.state.image);
            fetch(url, {
                body: form,
                credentials: 'same-origin',
                method: 'POST',
                mode: 'cors',
            })
                .then(response => response.json().then(value => console.log(value)))
        } else {
            alert("Photo has not been captured");
            return;
        }
    };

    checkPhotoDataExists(){
        let photo = document.getElementById('photo');
        let photodata = photo.toDataURL();
        let blankDataUrl = document.createElement("canvas").toDataURL();
        return photodata !== blankDataUrl;
    }

    handleDataAvailable(e) {
        if (e.data && e.data.size > 0) {
            recordedBlobs.push(e.data);
        }
    }

    render() {
        return (
            <div className="App">
                <div className="row">
                    <video id="camera" autoPlay playsInline>Cam</video>
                </div>
                <div className="row">
                    <button onClick={this.takePhoto}>Capture Target</button>
                    <button onClick={() => this.sendPhotoBlob(false)}>Index Face</button>
                    <button onClick={() => this.sendPhotoBlob(true)}>Check for Match</button>
                </div>
                <div className="row">
                    <canvas id="photo">canvas</canvas>
                </div>
            </div>
        );
    }
}

export default Webcam;
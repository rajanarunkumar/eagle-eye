import React, {Component} from 'react';
import Stomp from 'stompjs';
import {Row, Col} from 'reactstrap';
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
        this.connect();
    }

    handleData(data) {
        document.getElementById("imageMatch").src = "data:image/jpeg;base64," + data.bytes;
    }

    connect() {
        let stompClient;
        console.log("trying to connect");
        var socket = new SockJS('http://localhost:8080/eagle');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/images', function (message) {
                this.handleData(JSON.parse(message.body));
            }.bind(this));
        }.bind(this));
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
                let sid = prompt("Please enter the user's Full Name:");
                if(sid.length < 2) {
                    console.log("Full name must be entered.");
                    return;
                }
                url = "http://localhost:8080/image/s3upload?name=" + sid
            }
            let form = new FormData();
            form.append("file", this.state.image);
            fetch(url, {
                body: form,
                credentials: 'same-origin',
                method: 'POST'
            })
                .then(response => response.json().then(response => {alert(JSON.stringify(response))}));
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
            <div>
                    <Row>
                        <Col>
                        <img id="imageMatch" src="" alt=""></img>
                        </Col>
                        <Col>
                        <video id="camera" autoPlay playsInline>Cam</video>
                        </Col>
                    </Row>
                <Row>
                        <button onClick={this.takePhoto}>Capture Target</button>
                    <button onClick={() => this.sendPhotoBlob(false)}>Index Face</button>
                    <button onClick={() => this.sendPhotoBlob(true)}>Check for Match</button>
                </Row>
                <Row>
                    <canvas id="photo">canvas</canvas>
                </Row>
            </div>
        );
    }
}

export default Webcam;
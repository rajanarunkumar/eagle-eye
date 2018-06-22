import React, {Component} from 'react';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

var recordedBlobs = [];

class Webcam extends Component {
    constructor(props) {
        super(props);
        this.state = {
            recorder: '',
            mediaStream: '',
            mediaRecorder: '',
            image:''
        };
        this.gotLocalMediaStream = this.gotLocalMediaStream.bind(this);
        this.handleLocalMediaStreamError = this.handleLocalMediaStreamError.bind(this);
        this.takePhoto = this.takePhoto.bind(this);
        this.takeVideo = this.takeVideo.bind(this);
        this.sendPhotoBlob = this.sendPhotoBlob.bind(this);
        this.handleDataAvailable = this.handleDataAvailable.bind(this);
        this.stopVideo = this.stopVideo.bind(this);
        this.download = this.download.bind(this);
        this.handleData = this.handleData.bind(this);
        this.connect = this.connect.bind(this);
    }

    handleData(data){
        // TODO Recieve message and display image on screen
        console.log(data);
    }

    connect() {
        let stompClient;
        console.log("trying to connect");
        var socket = new SockJS('http://localhost:8080/gs-guide-websocket');
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

    componentDidMount() {
        this.setState({
            recorder: navigator.mediaDevices.getUserMedia({
                video: true,
            })
                .then(this.gotLocalMediaStream).catch(this.handleLocalMediaStreamError)
        });
        this.connect();
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
            .then(blob => this.setState({image:blob}));
        return image;
    }

    sendPhotoBlob(match) {
        let url = "http://localhost:8080/image/match";
        if(!match) {
            let sid = alert("Please enter the user's SID:");
            url = "http://localhost:8080/image/s3upload?name="+sid}
        let form = new FormData();
        form.append("file",this.state.image);
        fetch(url, {
            body: form,
            credentials: 'same-origin',
            method: 'POST',
            mode: 'cors',
        })
            .then(response => response.json().then(value => console.log(value)))// parses response to JSON
    }


    handleDataAvailable(e) {
        if (e.data && e.data.size > 0) {
            recordedBlobs.push(e.data);
        }
    }

    takeVideo() {
        let options = {mimeType: 'video/mp4;codecs=avc1'};
        let mediaRecorder;
        if (!MediaRecorder.isTypeSupported(options.mimeType)) {
            console.log(options.mimeType + ' is not Supported');
            options = {mimeType: 'video/x-matroska;codecs=avc1'};
            if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                console.log(options.mimeType + ' is not Supported');
                options = {mimeType: 'video/mp4;codecs=avc1'};
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    console.log(options.mimeType + ' is not Supported');
                    options = {mimeType: ''};
                }
            }
        }
        try {
            mediaRecorder = new MediaRecorder(this.state.mediaStream, options);
        } catch (e) {
            console.error('Exception while creating MediaRecorder: ' + e);
            alert('Exception while creating MediaRecorder: '
                + e + '. mimeType: ' + options.mimeType);
            return;
        }

        console.log('Created MediaRecorder', mediaRecorder, 'with options', options);
        mediaRecorder.ondataavailable = this.handleDataAvailable;
        mediaRecorder.start(10); // collect 10ms of data
        console.log('MediaRecorder started', mediaRecorder);
        this.setState({mediaRecorder: mediaRecorder});
        setTimeout(function () {
            this.stopVideo()
        }.bind(this), 5000);

    }

    stopVideo() {
        console.log("Video Recording stopped.");
        this.state.mediaRecorder.stop();
        console.log('Recorded Blobs: ', recordedBlobs);
    }

    download() {
        let blob = new Blob(recordedBlobs, {type: 'video/webm'});
        let url = window.URL.createObjectURL(blob);
        let a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = 'test.webm';
        document.body.appendChild(a);
        a.click();
        setTimeout(function () {
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        }, 100);
    }

    render() {
        return (
            <div className="App">

                <video id="camera" autoPlay playsInline>Cam</video>
                <button onClick={this.takePhoto}>Capture Target</button>
                <button onClick={this.sendPhotoBlob}>Upload Snapshot</button>
                <button onClick={() => this.sendPhotoBlob(true)}>Check for Match</button>
                <canvas id="photo">canvas</canvas>
            </div>
        );
    }
}

export default Webcam;
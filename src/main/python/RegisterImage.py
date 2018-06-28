from __future__ import print_function

import io
import sys

import boto3
from PIL import Image

s3 = boto3.client('s3', region_name='us-east-1')


def index_faces(bucket, key):
  rekognition = boto3.client('rekognition', region_name='us-east-1')
  response = rekognition.index_faces(
      Image={"S3Object":
               {"Bucket": bucket,
                "Name": key}},
      CollectionId="emp_collection")
  return response


def update_index(tableName, faceId, fullName, s3Key):
  dynamodb = boto3.client('dynamodb', region_name='us-east-1')
  response = dynamodb.put_item(
      TableName=tableName,
      Item={
        'RekognitionId': {'S': faceId},
        'FullName': {'S': fullName},
        'S3Key': {'S': s3Key}
      }
  )


constantImage = "/Users/arunrajan/Pictures/Picture/Iphone captures/IMG_0190.jpg"
print("Printing argument passed ", sys.argv)
# image = Image.open(sys.argv[1])
image = Image.open(constantImage)
stream = io.BytesIO()
image.save(stream, format="JPEG")
image_binary = stream.getvalue()

rekognition = boto3.client('rekognition', region_name='us-east-1')
dynamodb = boto3.client('dynamodb', region_name='us-east-1')
bucket = "eagle-eye-validator"
key = "index/1600mentzer.jpg"
indexFaceResponse = index_faces(bucket, key)
if indexFaceResponse['ResponseMetadata']['HTTPStatusCode'] == 200:
  faceId = indexFaceResponse['FaceRecords'][0]['Face']['FaceId']

  ret = s3.head_object(Bucket=bucket, Key=key)
  personFullName = ret['Metadata']['fullname']
update_index("emp_collection", faceId, personFullName, key)
print("Printing index face response ", indexFaceResponse)

response = rekognition.search_faces_by_image(
    CollectionId='emp_collection',
    Image={'Bytes': image_binary}
)

print('Printing Search Face by Image Response: ', response)

for match in response['FaceMatches']:
  print(match['Face']['FaceId'], match['Face']['Confidence'])
  face = dynamodb.get_item(
      TableName='emp_collection',
      Key={'RekognitionId': {'S': match['Face']['FaceId']}}
  )

  if 'Item' in face:
    print(face['Item']['FullName']['S'])
  else:
    print('no match found in person lookup')

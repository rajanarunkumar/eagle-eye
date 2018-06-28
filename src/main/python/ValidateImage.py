from __future__ import print_function

import io
import sys

import boto3
from PIL import Image

constantImage = "/Users/arunrajan/Pictures/arunv1.jpg"
print("Printing argument passed ", sys.argv)
# image = Image.open(sys.argv[1])
image = Image.open(constantImage)
stream = io.BytesIO()
image.save(stream, format="JPEG")
image_binary = stream.getvalue()

rekognition = boto3.client('rekognition', region_name='us-east-1')
dynamodb = boto3.client('dynamodb', region_name='us-east-1')
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
    print(face)
  else:
    print('no match found in person lookup')

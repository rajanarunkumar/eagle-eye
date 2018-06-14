import boto3
import io
from PIL import Image

rekognition = boto3.client('rekognition', region_name='us-east-2')
dynamodb = boto3.client('dynamodb', region_name='us-east-2')

image = Image.open("/Users/arunrajan/Pictures/Picture/Iphone captures/IMG_0190.jpg")
stream = io.BytesIO()
image.save(stream, format="JPEG")
image_binary = stream.getvalue()

response = rekognition.search_faces_by_image(
    CollectionId='emp_collection',
    Image={'Bytes': image_binary}
)
print ('Printing Search Face by Image Response: ', response)
for match in response['FaceMatches']:
  print (match['Face']['FaceId'], match['Face']['Confidence'])

  face = dynamodb.get_item(
      TableName='emp_collection',
      Key={'RekognitionId': {'S': match['Face']['FaceId']}}
  )

  if 'Item' in face:
    print (face['Item']['FullName']['S'])
  else:
    print ('no match found in person lookup')
